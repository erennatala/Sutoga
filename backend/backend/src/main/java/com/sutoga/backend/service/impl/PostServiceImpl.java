package com.sutoga.backend.service.impl;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.UserFriend;
import com.sutoga.backend.entity.mapper.PostMapper;
import com.sutoga.backend.entity.request.CreatePostRequest;
import com.sutoga.backend.entity.response.PostResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.CommentService;
import com.sutoga.backend.service.LikeService;
import com.sutoga.backend.service.PostService;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    @Lazy
    private final LikeService likeService;
    @Lazy
    private final CommentService commentService;
    private final PostMapper postMapper;
    private final AmazonS3 amazonS3;
    private final MinioClient minioClient;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${aws.cloudfront.domain-name}")
    private String cloudFrontDomainName;

    @Autowired
    public PostServiceImpl(@Lazy CommentService commentService, PostRepository postRepository, UserRepository userRepository, @Lazy LikeService likeService, PostMapper postMapper, MinioClient minioClient, AmazonS3 amazonS3) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeService = likeService;
        this.postMapper = postMapper;
        this.minioClient = minioClient;
        this.amazonS3 = amazonS3;
        this.commentService = commentService;
    }

//    @Autowired
//    private AmazonS3 s3Client;

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }


    @Override
    public PostResponse createPost(CreatePostRequest newPost) {
        Optional<User> user = userRepository.findById(newPost.getUserId());
        if (user.isEmpty()) {
            throw new ResultNotFoundException("User with id " + newPost.getUserId() + " not found!");
        }

        Post post = new Post();
        post.setPostDate(LocalDateTime.now());
        post.setDescription(newPost.getDescription());
        post.setUser(user.get());

        Post savedPost = postRepository.save(post);

        if (newPost.getMedia() != null) {
            handleMediaUpload(savedPost.getId(), newPost.getMedia());
        }
        PostResponse postResponse = postMapper.postToPostResponse(savedPost);

        postResponse.setLikeCount(0);
        postResponse.setCommentCount(0);
        postResponse.setPhotoUrl(user.get().getProfilePhotoUrl());

        return postResponse;
    }

    @Override
    public Post updatePost(Long postId, Post newPost) {
        Optional<Post> existingPost = postRepository.findById(postId);
        if (existingPost.isPresent()) {
            Post post = existingPost.get();
            post.setDescription(newPost.getDescription());
            post.setUser(newPost.getUser());
            post.setComments(newPost.getComments());
            return postRepository.save(post);
        }
        return null;
    }

    @Override
    public void deleteById(Long postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public Page<Post> getUserPosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        return postRepository.findByUser(user, PageRequest.of(pageNumber, pageSize, Sort.by("postDate").descending()));
    }

    @Override
    public Page<Post> getFriendsPosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        List<UserFriend> userFriends = user.getUserFriends();
        List<User> friends = userFriends.stream().map(UserFriend::getFriend).collect(Collectors.toList());
        List<Post> posts = new ArrayList<>();

        friends.forEach(friend -> {
            Page<Post> page = getUserPosts(friend.getId(), 0, Integer.MAX_VALUE);
            if(page != null) {
                posts.addAll(page.getContent());
            }
        });

        posts.sort((post1, post2) -> post2.getPostDate().compareTo(post1.getPostDate()));

        // Creating a page object
        int start = (int) PageRequest.of(pageNumber, pageSize).getOffset();
        int end = Math.min((start + PageRequest.of(pageNumber, pageSize).getPageSize()), posts.size());

        return new PageImpl<>(posts.subList(start, end), PageRequest.of(pageNumber, pageSize), posts.size());
    }

    @Override
    public Page<PostResponse> getMergedPosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        List<UserFriend> userFriends = user.getUserFriends();
        List<User> friends = userFriends.stream().map(UserFriend::getFriend).collect(Collectors.toList());
        List<Post> posts = new ArrayList<>();

        // Fetch user's posts
        Page<Post> userPosts = getUserPosts(userId, 0, Integer.MAX_VALUE);
        if (userPosts != null) {
            posts.addAll(userPosts.getContent());
        }

        // Fetch each friend's posts
        friends.forEach(friend -> {
            Page<Post> friendPosts = getUserPosts(friend.getId(), 0, Integer.MAX_VALUE);
            if (friendPosts != null) {
                posts.addAll(friendPosts.getContent());
            }
        });

        posts.sort((post1, post2) -> post2.getPostDate().compareTo(post1.getPostDate()));

        int start = (int) PageRequest.of(pageNumber, pageSize).getOffset();
        int end = Math.min((start + PageRequest.of(pageNumber, pageSize).getPageSize()), posts.size());

        if (start > end) {
            start = end;
        }

        List<PostResponse> postResponses = posts.subList(start, end)
                .stream()
                .map(post -> {
                    PostResponse postResponse = postMapper.postToPostResponse(post);
                    postResponse.setMediaUrl(post.getMediaUrl());
                    postResponse.setLikeCount(post.getLikes().size());
                    postResponse.setCommentCount(post.getComments().size());
                    postResponse.setLikedByUser(likeService.isPostLikedByUser(post.getId(), userId));
                    postResponse.setUsersPost(post.getUser().getId().equals(userId));
                    postResponse.setPhotoUrl(post.getUser().getProfilePhotoUrl());
                    return postResponse;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(postResponses, PageRequest.of(pageNumber, pageSize), posts.size());
    }

    @Override
    public Page<PostResponse> getProfilePosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        Page<Post> userPosts = getUserPosts(userId, pageNumber, pageSize);

        List<PostResponse> postResponses = userPosts.getContent()
                .stream()
                .map(post -> {
                    PostResponse postResponse = postMapper.postToPostResponse(post);
                    postResponse.setMediaUrl(post.getMediaUrl()); // Set the mediaUrl
                    postResponse.setUserId(user.getId()); // Set the userId
                    postResponse.setUsername(user.getUsername()); // Set the username
                    postResponse.setLikeCount(post.getLikes().size()); // Set the likeCount
                    postResponse.setCommentCount(post.getComments().size());
                    // Set the isLiked field based on the user's like status
                    postResponse.setLikedByUser(likeService.isPostLikedByUser(post.getId(), userId));
                    postResponse.setUsersPost(true);
                    postResponse.setPhotoUrl(user.getProfilePhotoUrl());
                    return postResponse;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(postResponses, userPosts.getPageable(), userPosts.getTotalElements());
    }

    public Post handleMediaUpload(Long postId, MultipartFile file) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        String mediaUrl = uploadMediaToMinioAndGenerateUrl(file);
        //String mediaUrl = uploadMediaToS3(file);

        post.setMediaUrl(mediaUrl);

        return postRepository.save(post);
    }

    public String uploadMediaToMinioAndGenerateUrl(MultipartFile file) {
        String objectName = generateUniqueMediaName(file.getOriginalFilename());
        String bucketName = s3BucketName;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            String mediaUrl = cloudFrontDomainName + "/" + bucketName + "/" + objectName;
            return mediaUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading media to MinIO server", e);
        }
    }

//    private String generateUniqueMediaName(String originalFilename) {
//        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        return UUID.randomUUID().toString() + extension;
//    }

    public InputStream getMediaAsStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(s3BucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving media from MinIO server", e);
        }
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    @Transactional
    @Override
    public void deletePost(Long postId) {
        likeService.deleteLikesByPostId(postId);
        commentService.deleteCommentsByPostId(postId);
        postRepository.deleteById(postId);
    }

    @Override
    public Page<PostResponse> getUserLikedPosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        Page<Like> userLikedPosts = likeService.getUserLikedPosts(userId, PageRequest.of(pageNumber, pageSize, Sort.by("likeDate").descending()));
        List<Post> posts = userLikedPosts.stream().map(Like::getPost).collect(Collectors.toList());

        int start = (int) userLikedPosts.getPageable().getOffset();
        int end = Math.min((start + userLikedPosts.getPageable().getPageSize()), posts.size());

        if (start > end) {
            start = end;
        }

        List<PostResponse> postResponses = posts.subList(start, end)
                .stream()
                .map(post -> {
                    PostResponse postResponse = postMapper.postToPostResponse(post);
                    postResponse.setMediaUrl(post.getMediaUrl());
                    postResponse.setLikeCount(post.getLikes().size());
                    postResponse.setCommentCount(post.getComments().size());
                    postResponse.setLikedByUser(true);
                    postResponse.setUsersPost(post.getUser().getId().equals(userId));
                    postResponse.setPhotoUrl(post.getUser().getProfilePhotoUrl());
                    return postResponse;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(postResponses, userLikedPosts.getPageable(), userLikedPosts.getTotalElements());
    }

    public String uploadMediaToS3(MultipartFile file) {
        String objectKey = generateUniqueMediaName(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(s3BucketName, objectKey, inputStream, metadata));

            return getObjectUrl(objectKey);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading media to S3", e);
        }
    }

    private String generateUniqueMediaName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueName = UUID.randomUUID().toString();
        return "media/" + uniqueName + extension;
    }

    private String getObjectUrl(String objectKey) {
        return "https://" + cloudFrontDomainName + "/" + objectKey;
    }

}

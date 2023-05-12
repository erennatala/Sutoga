package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.request.CreatePostRequest;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.PostService;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private AmazonS3 s3Client;

    @Autowired
    private MinioClient minioClient;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${aws.cloudfront.domain-name}")
    private String cloudFrontDomainName;

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }


    @Override
    public Post createPost(CreatePostRequest newPost) {
        Optional<User> user = userRepository.findById(newPost.getUserId());
        if(user.isEmpty()) {
            throw new ResultNotFoundException("User with id "+ newPost.getUserId() +" not found!");
        }

        Post post = new Post();
        post.setPostDate(LocalDateTime.now());
        post.setDescription(newPost.getDescription());
        post.setUser(user.get());

        Post savedPost = postRepository.save(post);

        // If a media file is included in the request, upload it to MinIO
        if (newPost.getMedia() != null) {
            handleMediaUpload(savedPost.getId(), newPost.getMedia());
        }

        return savedPost;
    }

    @Override
    public Post getOnePostById(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElse(null);
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

        return postRepository.findByUser(user, PageRequest.of(pageNumber, pageSize, Sort.by("publishDate").descending()));
    }

    @Override
    public Page<Post> getFriendsPosts(Long userId, int pageNumber, int pageSize) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        List<User> friends = user.getFriends();
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

    public Post handleMediaUpload(Long postId, MultipartFile file) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));

        // Upload the media file to the MinIO server and generate the URL
        String mediaUrl = uploadMediaToMinioAndGenerateUrl(file);

        // Add the media URL to the mediaUrls list in the Post entity
        List<String> mediaUrls = post.getMediaUrls();
        mediaUrls.add(mediaUrl);
        post.setMediaUrls(mediaUrls);

        // Save the updated Post entity to the database
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

            return cloudFrontDomainName + "/" + objectName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading media to MinIO server", e);
        }
    }

    private String generateUniqueMediaName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

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
}

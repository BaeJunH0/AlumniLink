package com.sparksInTheStep.webBoard.post.application;

import com.sparksInTheStep.webBoard.global.errorHandling.CustomException;
import com.sparksInTheStep.webBoard.global.errorHandling.errorCode.PostErrorCode;
import com.sparksInTheStep.webBoard.member.application.dto.MemberInfo;
import com.sparksInTheStep.webBoard.member.domain.Member;
import com.sparksInTheStep.webBoard.member.persistent.MemberRepository;
import com.sparksInTheStep.webBoard.post.domain.Post;
import com.sparksInTheStep.webBoard.post.persistence.PostRepository;
import com.sparksInTheStep.webBoard.post.application.dto.PostCommand;
import com.sparksInTheStep.webBoard.post.application.dto.PostInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createPost(String nickname, PostCommand postCommand) {
        Member member = memberRepository.findByNickname(nickname);

        postRepository.save(Post.from(postCommand, member));
    }

    @Transactional(readOnly = true)
    public Page<PostInfo> getPostsByMember(MemberInfo.Default memberInfo, Pageable pageable) {
        Member member = memberRepository.findByNickname(memberInfo.nickname());
        Page<Post> posts = postRepository.findByMember(member, pageable);

        return posts.map(PostInfo::from);
    }

    @Transactional(readOnly = true)
    public Page<PostInfo> getAllPosts(Pageable pageable){
        return postRepository.findAll(pageable).map(PostInfo::from);
    }

    @Transactional(readOnly = true)
    public PostInfo getOnePost(Long id){
        return PostInfo.from(postRepository.findById(id).orElseThrow(
                ()-> CustomException.of(PostErrorCode.NOT_FOUND)
        ));
    }

    @Transactional
    public void updatePost(MemberInfo.Default memberInfo, Long postId, PostCommand postCommand){
        Member member = memberRepository.findByNickname(memberInfo.nickname());
        Post post = postRepository.findPostById(postId).orElseThrow(
                () -> CustomException.of(PostErrorCode.NOT_FOUND)
        );

        post.update(
                postCommand.title(), postCommand.tag(), postCommand.body(), postCommand.description()
        );
    }

    @Transactional
    public void deletePost(MemberInfo.Default memberInfo, Long postId) {
        Member member = memberRepository.findByNickname(memberInfo.nickname());
        Post post = postRepository.findPostById(postId).orElseThrow(
                () -> CustomException.of(PostErrorCode.NOT_FOUND)
        );

        // 게시물 작성자 체크
        if(post.getMember() != member){
            throw CustomException.of(PostErrorCode.NOT_MY_COMMENT);
        }

        postRepository.delete(post);
    }
}

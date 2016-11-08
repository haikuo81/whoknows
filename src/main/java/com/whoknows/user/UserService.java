package com.whoknows.user;

import com.whoknows.comment.CommentService;
import com.whoknows.domain.Reply;
import com.whoknows.domain.Tag;
import com.whoknows.domain.TargetType;
import com.whoknows.domain.Topic;
import com.whoknows.domain.User;
import com.whoknows.follow.FollowService;
import com.whoknows.like.LikeService;
import com.whoknows.message.password.ResetPasswdRequest;
import com.whoknows.reply.RelpyService;
import com.whoknows.reply.ReplyDetail;
import com.whoknows.search.Paging;
import com.whoknows.topic.TopicDetail;
import com.whoknows.search.TopicResult;
import com.whoknows.topic.TopicService;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final int pageSize = 20;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TopicService topicService;
	@Autowired
	private RelpyService relpyService;
	@Autowired
	private LikeService likeService;
	@Autowired
	private FollowService followService;
	@Autowired
	private CommentService commentService;

	public User currentUser() {
		if (SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal() instanceof User) {
			return (User) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal();
		} else {
			return null;
		}
	}

	public UserDetail getUser(Long id) {
		if (id == null) {
			return null;
		}

		try {
			UserDetail userView = new UserDetail();
			userView.setUser(userRepository.getUserById(id), userRepository.getUserRolesByUserId(id));
			return userView;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public List<TopicResult> getUserTopic(Long id) {
		if (id == null) {
			return null;
		}

		try {
			return userRepository.getUserTopic(id).stream().map(topic -> {
				TopicResult topicResult = new TopicResult();
				topicResult.setTopicDetail(new TopicDetail());
				topicResult.getTopicDetail().setTopic(topic);
				return topicResult;
			}).collect(Collectors.toList());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public boolean createUser(User user) {
		if (StringUtils.isEmpty(user.getEmail()) && StringUtils.isEmpty(user.getPasswd())) {
			return false;
		}
		try {
			userRepository.createUser(user);
			log.info("Create user :{} success.", user.getEmail());
			return true;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return false;
		}
	}

	public boolean resetPasswd(ResetPasswdRequest request) {
		if (request == null || StringUtils.isEmpty(request.getEmail())
				|| StringUtils.isEmpty(request.getOldPasswd())
				|| StringUtils.isEmpty(request.getNewPasswd())
				|| StringUtils.isEmpty(request.getRepeatNewPasswd())
				|| !StringUtils.equals(request.getNewPasswd(), request.getRepeatNewPasswd())) {
			return false;
		}

		if (!userRepository.validUserByEmailAndPasswd(request.getEmail(), request.getOldPasswd())) {
			return false;
		}

		try {
			userRepository.resetPasswd(request);
			return true;
		} catch (Exception e) {
			log.error("Reset passwd error , username:{}, {}", request.getEmail(), e);
			return false;
		}
	}

	public boolean editUserInfo(User user) {
		if (user.getId() == null) {
			return false;
		}

		try {
			userRepository.editUserInfo(user);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public UserTopicResponse getUserCreateTopics(Long userID, int page) {
		try {
			UserTopicResponse userTopicResponse = new UserTopicResponse();
			Paging paging = new Paging();
			paging.setCurrentPage(page);
			paging.setPerPage(pageSize);
			int commentCount = userRepository.getUserCreateTopicCount(userID);
			paging.setTotalPage(commentCount % pageSize == 0 ? commentCount / pageSize : commentCount / pageSize + 1);
			userTopicResponse.setPaging(paging);

			userTopicResponse.setTopicResults(userRepository.getUserCreateTopics(userID, page, pageSize).parallelStream().map(topic -> {
				TopicResult topicResult = new TopicResult();
				TopicDetail topicDetail = new TopicDetail();
				topicDetail.setTopic(topic);
				topicDetail.setAuthor(getUser(topic.getId()));
				topicDetail.setFollowCount(followService.followCount(topic.getId(), TargetType.topic));
				topicDetail.setCurrentFollowed(followService.isFollowed(userID, topic.getId(), TargetType.topic));
				topicDetail.setCurrentLiked(likeService.isLiked(userID, topic.getId(), TargetType.topic));
				topicResult.setTopicDetail(topicDetail);

				Reply reply = relpyService.getHotReplyForRopic(topic.getId());
				if (reply != null) {
					ReplyDetail replyDetail = new ReplyDetail();
					replyDetail.setReply(reply);
					replyDetail.setAuthor(getUser(reply.getUser_id()));
					replyDetail.setLikeCount(likeService.likeCount(reply.getId(), TargetType.reply));
					replyDetail.setCommentCount(commentService.commentCount(reply.getId()));
					replyDetail.setCurrentLiked(likeService.isLiked(userID, reply.getId(), TargetType.reply));
					topicResult.setReplyDetail(replyDetail);
				}
				return topicResult;
			}).collect(Collectors.toList()));
			return userTopicResponse;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public UserTopicResponse getUserFollowTopics(Long userID, int page) {
		try {
			UserTopicResponse userTopicResponse = new UserTopicResponse();
			Paging paging = new Paging();
			paging.setCurrentPage(page);
			paging.setPerPage(pageSize);
			int commentCount = userRepository.getUserFollowTopicCount(userID);
			paging.setTotalPage(commentCount % pageSize == 0 ? commentCount / pageSize : commentCount / pageSize + 1);
			userTopicResponse.setPaging(paging);

			userTopicResponse.setTopicResults(userRepository.getUserFollowTopics(userID, page, pageSize).parallelStream().map(topic -> {
				TopicResult topicResult = new TopicResult();
				TopicDetail topicDetail = new TopicDetail();
				topicDetail.setTopic(topic);
				topicDetail.setAuthor(getUser(topic.getId()));
				topicDetail.setFollowCount(followService.followCount(topic.getId(), TargetType.topic));
				topicDetail.setCurrentFollowed(followService.isFollowed(userID, topic.getId(), TargetType.topic));
				topicDetail.setCurrentLiked(likeService.isLiked(userID, topic.getId(), TargetType.topic));
				topicResult.setTopicDetail(topicDetail);

				Reply reply = relpyService.getHotReplyForRopic(topic.getId());
				if (reply != null) {
					ReplyDetail replyDetail = new ReplyDetail();
					replyDetail.setReply(reply);
					replyDetail.setAuthor(getUser(reply.getUser_id()));
					replyDetail.setLikeCount(likeService.likeCount(reply.getId(), TargetType.reply));
					replyDetail.setCommentCount(commentService.commentCount(reply.getId()));
					replyDetail.setCurrentLiked(likeService.isLiked(userID, reply.getId(), TargetType.reply));
					topicResult.setReplyDetail(replyDetail);
				}
				return topicResult;
			}).collect(Collectors.toList()));
			return userTopicResponse;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public UserTopicResponse getUserReplyTopics(Long userID, int page) {
		try {
			UserTopicResponse userTopicResponse = new UserTopicResponse();
			Paging paging = new Paging();
			paging.setCurrentPage(page);
			paging.setPerPage(pageSize);
			int commentCount = userRepository.getUserReplyCount(userID);
			paging.setTotalPage(commentCount % pageSize == 0 ? commentCount / pageSize : commentCount / pageSize + 1);
			userTopicResponse.setPaging(paging);

			userTopicResponse.setTopicResults(userRepository.getUserReplys(userID, page, pageSize).parallelStream().map(reply -> {
				TopicResult topicResult = new TopicResult();
				Topic topic = topicService.getTopic(reply.getTopic_id());
				if (topic != null) {
					TopicDetail topicDetail = new TopicDetail();
					topicDetail.setTopic(topic);
					topicDetail.setAuthor(getUser(topic.getId()));
					topicDetail.setFollowCount(followService.followCount(topic.getId(), TargetType.topic));
					topicDetail.setCurrentFollowed(followService.isFollowed(userID, topic.getId(), TargetType.topic));
					topicDetail.setCurrentLiked(likeService.isLiked(userID, topic.getId(), TargetType.topic));
					topicResult.setTopicDetail(topicDetail);
				} else {
					return null;
				}

				ReplyDetail replyDetail = new ReplyDetail();
				replyDetail.setReply(reply);
				replyDetail.setAuthor(getUser(reply.getUser_id()));
				replyDetail.setLikeCount(likeService.likeCount(reply.getId(), TargetType.reply));
				replyDetail.setCommentCount(commentService.commentCount(reply.getId()));
				replyDetail.setCurrentLiked(likeService.isLiked(userID, reply.getId(), TargetType.reply));
				topicResult.setReplyDetail(replyDetail);
				return topicResult;
			}).filter(topicResult -> topicResult != null).collect(Collectors.toList()));
			return userTopicResponse;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public List<Tag> getUserTagList() {
		try {
			User user = currentUser();
			if (user != null && user.getId() != null) {
				return userRepository.getUserTagList(user.getId());
			}
			return null;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return null;
		}
	}

	public UserConutInfoResponse getUserCountInfo(Long userId) {
		UserConutInfoResponse userConutInfoResponse = new UserConutInfoResponse();
		userConutInfoResponse.setReplyCount(userRepository.getUserReplyCount(userId));
		userConutInfoResponse.setCreateTopicCount(userRepository.getUserCreateTopicCount(userId));
		userConutInfoResponse.setFollowCount(userRepository.getUserFollowTopicCount(userId));
		return userConutInfoResponse;
	}

}

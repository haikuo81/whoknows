package com.whoknows.user;

import com.whoknows.domain.Reply;
import com.whoknows.domain.Role;
import com.whoknows.domain.Tag;
import com.whoknows.domain.Topic;
import com.whoknows.domain.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder encoder;

	public User getUserById(Long id) {
		return jdbcTemplate.query("select * from user where id = ? limit 1",
				ps -> ps.setLong(1, id),
				(rs, row) -> {
					User user = new User();
					user.setId(rs.getLong("id"));
					user.setEmail(rs.getString("email"));
					user.setPhone(rs.getString("phone"));
					user.setPasswd(rs.getString("passwd"));
					user.setePass(rs.getString("e_pass"));
					user.setFirstName(rs.getString("first_name"));
					user.setLastName(rs.getString("last_name"));
					user.setCompanyName(rs.getString("company_name"));
					user.setProvince(rs.getString("province"));
					user.setCity(rs.getString("city"));
					user.setAddress(rs.getString("address"));
					user.setCreateTime(rs.getTimestamp("create_time"));
					user.setUpdateTime(rs.getTimestamp("update_time"));
					user.setVip(rs.getBoolean("vip"));
					user.setPicture(rs.getString("picture"));
					user.setEducation(rs.getString("education"));
					user.setSignature(rs.getString("signature"));
					user.setTitle(rs.getString("title"));
					user.setAction(rs.getString("action"));
					return user;
				}).stream().findAny().orElse(null);
	}

	public List<Topic> getUserTopic(Long id) {
		return jdbcTemplate.query("select * from topic where user_id = ? ",
				ps -> ps.setLong(1, id),
				(rs, row) -> {
					Topic topic = new Topic();
					topic.setId(rs.getLong("id"));
					topic.setAction(rs.getString("action"));
					topic.setTitle(rs.getString("title"));
					topic.setContent(rs.getString("content"));
					topic.setRank(rs.getLong("rank"));
					topic.setCreate_time(rs.getTimestamp("create_time"));
					topic.setUpdate_time(rs.getTimestamp("update_time"));
					return topic;
				});
	}

	public void createUser(User user) {
		jdbcTemplate.update("insert into user(email,phone,e_pass) values (?, ?, ?)",
				ps -> {
					ps.setString(1, user.getEmail());
					ps.setString(2, "");
					ps.setString(3, encoder.encode(user.getPasswd()));
				});
	}

	public void editUserInfo(User user) {
		jdbcTemplate.update("update user set phone = ?,  first_name = ? , last_name = ? , company_name = ? , "
				+ "province = ? , city = ? , address = ? , education = ?, title = ? , signature = ? "
				+ "where id = ? ",
				ps -> {
					ps.setString(1, user.getPhone());
					ps.setString(2, user.getFirstName());
					ps.setString(3, user.getLastName());
					ps.setString(4, user.getCompanyName());
					ps.setString(5, user.getProvince());
					ps.setString(6, user.getCity());
					ps.setString(7, user.getAddress());
					ps.setString(8, user.getEducation());
					ps.setString(9, user.getTitle());
					ps.setLong(10, user.getId());
					ps.setString(11, user.getSignature());
				});
	}

	public boolean validUserByEmailAndPasswd(String email, String oldPasswd) {
		try {
			String ePass = jdbcTemplate.queryForObject("select e_pass from user where email = ? limit 1",
					new Object[]{email}, new SingleColumnRowMapper<String>(String.class));
			return encoder.matches(oldPasswd, ePass);
		} catch (Exception e) {
			return false;
		}
	}

	public void resetPasswd(ResetPasswdRequest request) {

		jdbcTemplate.update("update user set e_pass = ? where email = ?",
				ps -> {
					ps.setString(1, encoder.encode(request.getNewPasswd()));
					ps.setString(2, request.getEmail());
				});
	}

	public List<Role> getUserRolesByUserId(Long id) {
		return jdbcTemplate.query("select role.* from role "
				+ "left join user_role on role.id = user_role.role_id "
				+ "where user_role.user_id = ?",
				ps -> ps.setLong(1, id),
				(rs, row) -> {
					Role role = new Role();
					role.setId(rs.getLong("id"));
					role.setRole(rs.getString("role"));
					return role;
				});
	}

	public List<Topic> getUserCreateTopics(Long user_id, int page, int pageSize) {
		return jdbcTemplate.query("select * from topic where user_id = ? "
				+ "order by id desc "
				+ "limit ? OFFSET ? ",
				ps -> {
					ps.setLong(1, user_id);
					ps.setInt(2, pageSize);
					ps.setInt(3, (page - 1) * pageSize);
				}, (rs, row) -> {
					Topic topic = new Topic();
					topic.setId(rs.getLong("id"));
					topic.setUser_id(rs.getLong("user_id"));
					topic.setAction(rs.getString("action"));
					topic.setTitle(rs.getString("title"));
					topic.setContent(rs.getString("content"));
					topic.setRank(rs.getLong("rank"));
					topic.setCreate_time(rs.getTimestamp("create_time"));
					topic.setUpdate_time(rs.getTimestamp("update_time"));
					return topic;
				});
	}

	public Integer getUserCreateTopicCount(Long user_id) {
		return jdbcTemplate.query("select count(1) from topic where user_id = ? ",
				ps -> {
					ps.setLong(1, user_id);
				},
				(rs, row) -> {
					return rs.getInt("count(1)");
				}).stream().findAny().orElse(null);
	}

	public List<Topic> getUserFollowTopics(Long user_id, int page, int pageSize) {
		return jdbcTemplate.query("select * from topic where id in (	"
				+ "select target_id from follow where user_id = ? and target_type = 'topic' ) "
				+ "order by id desc "
				+ "limit ? OFFSET ? ",
				ps -> {
					ps.setLong(1, user_id);
					ps.setInt(2, pageSize);
					ps.setInt(3, (page - 1) * pageSize);
				}, (rs, row) -> {
					Topic topic = new Topic();
					topic.setId(rs.getLong("id"));
					topic.setUser_id(rs.getLong("user_id"));
					topic.setAction(rs.getString("action"));
					topic.setTitle(rs.getString("title"));
					topic.setContent(rs.getString("content"));
					topic.setRank(rs.getLong("rank"));
					topic.setCreate_time(rs.getTimestamp("create_time"));
					topic.setUpdate_time(rs.getTimestamp("update_time"));
					return topic;
				});
	}

	public Integer getUserFollowTopicCount(Long user_id) {
		return jdbcTemplate.query("select count(1) from topic where id in (	"
				+ "select target_id from follow where user_id = ? and target_type = 'topic' ) ",
				ps -> {
					ps.setLong(1, user_id);
				},
				(rs, row) -> {
					return rs.getInt("count(1)");
				}).stream().findAny().orElse(null);
	}

	public List<Reply> getUserReplys(Long user_id, int page, int pageSize) {
		return jdbcTemplate.query("select * from reply where user_id = ? "
				+ "order by id desc "
				+ "limit ? OFFSET ? ",
				ps -> {
					ps.setLong(1, user_id);
					ps.setInt(2, pageSize);
					ps.setInt(3, (page - 1) * pageSize);
				},
				(rs, row) -> {
					Reply reply = new Reply();
					reply.setId(rs.getLong("id"));
					reply.setAction(rs.getString("action"));
					reply.setContent(rs.getString("content"));
					reply.setCreate_time(rs.getTimestamp("create_time"));
					reply.setReply_id(rs.getLong("reply_id"));
					reply.setTopic_id(rs.getLong("topic_id"));
					reply.setUpdate_time(rs.getTimestamp("update_time"));
					reply.setUser_id(rs.getLong("user_id"));
					return reply;
				});
	}

	public Integer getUserReplyCount(Long user_id) {
		return jdbcTemplate.query("select count(1) from reply where user_id = ?  ",
				ps -> {
					ps.setLong(1, user_id);
				},
				(rs, row) -> {
					return rs.getInt("count(1)");
				}).stream().findAny().orElse(null);
	}

	public List<Tag> getUserTagList(Long userId) {
		return jdbcTemplate.query("select * from tag where id in "
				+ "( select target_id from follow "
				+ "left join user on follow.user_id = user.id "
				+ "where follow.target_type = 'tag' "
				+ "and user.id = ? ) "
				+ "order by rank desc;",
				ps -> {
					ps.setLong(1, userId);
				},
				(rs, row) -> {
					Tag tag = new Tag();
					tag.setId(rs.getLong("id"));
					tag.setName(rs.getString("name"));
					tag.setAction(rs.getString("action"));
					tag.setRank(rs.getLong("rank"));
					return tag;
				});
	}
}

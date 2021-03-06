package com.whoknows.tag;

import com.whoknows.domain.Tag;
import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/tag")
public class TagController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TagService tagService;

	@RequestMapping(path = "/{tagId}", method = RequestMethod.GET)
	public ResponseEntity getTag(@PathVariable("tagId") Long tagId) {
		Tag tags = tagService.getTagByID(tagId);
		if (tags != null) {
			return ResponseEntity.ok(tags);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@RequestMapping(method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity addTag(@RequestBody Tag tag) {
		log.info(ToStringBuilder.reflectionToString(tag, ToStringStyle.MULTI_LINE_STYLE));
		if (tagService.addTag(tag)) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity deleteTag(@RequestBody Tag tag) {
		log.info(ToStringBuilder.reflectionToString(tag, ToStringStyle.MULTI_LINE_STYLE));
		if (tagService.deleteTag(tag)) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@RequestMapping(path = "/list", method = RequestMethod.GET)
	public ResponseEntity getTagList() {
		List<TagSelect> tags = tagService.getTagList();
		if (tags != null) {
			return ResponseEntity.ok(tags);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@RequestMapping(path = "/list/{tagName}", method = RequestMethod.GET)
	public ResponseEntity getTagList(@PathVariable("tagName") String tagName) {
		List<Tag> tags = tagService.getTagList(tagName);
		if (tags != null) {
			return ResponseEntity.ok(tags);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@RequestMapping(path = "/home/{tagId}/{page}", method = RequestMethod.GET)
	public ResponseEntity getTagHome(@PathVariable("tagId") Long tagId, @PathVariable("page") Integer page) {
		TagHomeRespone tagHomeRespone = tagService.getTagHome(tagId, page);
		if (tagHomeRespone != null) {
			return ResponseEntity.ok(tagHomeRespone);
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
}

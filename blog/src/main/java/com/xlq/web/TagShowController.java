package com.xlq.web;


import com.xlq.po.Tag;
import com.xlq.po.Type;
import com.xlq.service.BlogService;
import com.xlq.service.TagService;
import com.xlq.service.TypeService;
import com.xlq.vo.BlogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import java.util.List;

@Controller
public class TagShowController {

    @Autowired
    private TagService tagService;

    @Autowired
    private BlogService blogService;

    @Transactional
    @GetMapping("/tags/{id}")
    public String tags(@PageableDefault(size = 8, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                       @PathVariable Long id, Model model) {
        List<Tag> tagBlogs = tagService.listTagTop(1000);
        if (id == -1) {
            id = tagBlogs.get(0).getId();
        }
        model.addAttribute("tags", tagBlogs);
        model.addAttribute("page", blogService.listBlog(id, pageable));
        model.addAttribute("activeTagId", id);
        return "tags";
    }

}

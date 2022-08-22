package com.ace.ailpv.controller;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ace.ailpv.entity.Batch;
import com.ace.ailpv.entity.BatchHasResource;
import com.ace.ailpv.entity.Course;
import com.ace.ailpv.entity.User;
import com.ace.ailpv.service.BatchHasResourceService;
import com.ace.ailpv.service.UserService;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private UserService userService;

    @Autowired
    private BatchHasResourceService batchHasResourceService;

    @GetMapping("/student-table")
    public String setupStudentTable(ModelMap model, HttpSession session) {
        User teacher = (User) session.getAttribute("userInfo");
        model.addAttribute("studentList", userService.getStudentListByTeacherId(teacher.getId()));
        return "/teacher/TCH-STB-11";
    }

    @GetMapping("/uploadResource")
    public String setupUploadResource(ModelMap model, HttpSession session) {
        User teacherInfo = (User) session.getAttribute("userInfo");
        Set<Course> teacherCourseList = userService.getTeacherCourseListById(teacherInfo.getId());
        model.addAttribute("teacherCourseList", teacherCourseList);
        return "/teacher/TCH-ULR-02";
    }

    @GetMapping("/uploadResourceForm/{resourceId}/{courseId}/{resourceName}")
    public String setupUploadResourceForm(
            @PathVariable("resourceId") Long resourceId,
            @PathVariable("courseId") Long courseId,
            @PathVariable("resourceName") String resourceName,
            ModelMap model, HttpSession session) {
        User teacherInfo = (User) session.getAttribute("userInfo");
        Set<Batch> teacherBathList = userService.getTeacherBatchListByTeacherIdAndCourseId(teacherInfo.getId(),
                courseId);
        model.addAttribute("teacherBatchList", teacherBathList);
        model.addAttribute("resourceId", resourceId);
        model.addAttribute("resourceName", resourceName);
        model.addAttribute("batchHasResource", new BatchHasResource());
        return "/teacher/TCH-SDR-03";
    }

    @PostMapping("/uploadResourceForBatch")
    public String uploadResourceForBatch(
            @ModelAttribute("BatchHasResource") BatchHasResource batchHasResource,
            RedirectAttributes redirectAttrs) {
        BatchHasResource resBatchHasResource = batchHasResourceService.getBatchHasResourceByBatchIdAndResourceId(
                batchHasResource.getBatch().getId(), batchHasResource.getResource().getId());
        if (resBatchHasResource != null) {
            batchHasResourceService.deleteBatchHasResourceById(resBatchHasResource.getId());
        }
        batchHasResourceService.addBatchHasResource(batchHasResource);
        redirectAttrs.addFlashAttribute("msg", "Successfully Uploaded");
        return "redirect:/teacher/uploadResource";
    }

    @GetMapping("/teacher-public-chat")
    public String setupTeacherPublicChat(HttpSession session, ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        User teacherInfo = userService.getUserById(teacherId);
        List<Batch> batchList = userService.getTeacherBatchListById(teacherInfo.getId());
        Batch firstBatch = batchList.get(0);
        model.addAttribute("userId", teacherInfo.getId());
        model.addAttribute("username", teacherInfo.getName());
        model.addAttribute("batchId", firstBatch.getId());
        model.addAttribute("batchName", firstBatch.getName());
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-PBC-05";
    }

    @GetMapping("/chatWithBatch/{batchId}/{batchName}")
    public String setupChatWithBatch(
            @PathVariable("batchId") Long batchId,
            @PathVariable("batchName") String batchName,
            HttpSession session,
            ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        User teacherInfo = userService.getUserById(teacherId);
        model.addAttribute("userId", teacherInfo.getId());
        model.addAttribute("username", teacherInfo.getName());
        model.addAttribute("batchId", batchId);
        model.addAttribute("batchName", batchName);
        List<Batch> batchList = userService.getTeacherBatchListById(teacherInfo.getId());
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-CWB-06";
    }

}

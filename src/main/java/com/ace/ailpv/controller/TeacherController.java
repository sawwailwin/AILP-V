package com.ace.ailpv.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ace.ailpv.entity.Assignment;
import com.ace.ailpv.entity.AssignmentAnswer;
import com.ace.ailpv.entity.Batch;
import com.ace.ailpv.entity.Schedule;
import com.ace.ailpv.entity.User;
import com.ace.ailpv.entity.UserSchedule;
import com.ace.ailpv.service.AssignmentAnswerService;
import com.ace.ailpv.service.AssignmentService;
import com.ace.ailpv.service.ScheduleService;
import com.ace.ailpv.service.UserScheduleService;
import com.ace.ailpv.service.UserService;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserScheduleService userScheduleService;

    @Autowired
    private ScheduleService scheduleService;

    //added by me
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AssignmentAnswerService assignmentAnswerService;

    @GetMapping("/dashboard")
    public String setupTeacherDashboard(ModelMap model, HttpSession session) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        batchList = batchList.stream()
                .filter(batch -> batch.getIsActive())
                .collect(Collectors.toList());
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-DSB-01";
    }

    @GetMapping("/student-table")
    public String setupStudentTable(ModelMap model, HttpSession session) {
        String teacherId = (String) session.getAttribute("uid");
        User teacherInfo = userService.getUserById(teacherId);
        model.addAttribute("studentList", userService.getStudentListByTeacherId(teacherInfo.getId()));
        return "/teacher/TCH-STB-11";
    }

    @GetMapping("/teacher-public-chat")
    public String setupTeacherPublicChat(HttpSession session, ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        User teacherInfo = userService.getUserById(teacherId);
        List<Batch> batchList = userService.getTeacherBatchListById(teacherInfo.getId());
        batchList = batchList.stream().filter(batch -> batch.getIsActive()).collect(Collectors.toList());
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
        batchList = batchList.stream().filter(batch -> batch.getIsActive()).collect(Collectors.toList());
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-CWB-06";
    }

    @GetMapping("/modifyAttendance")
    public String setupModifyAttendance(ModelMap model, HttpSession session) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-MDA-07";
    }

    @GetMapping("/attendance-table")
    public String setupAttendanceTable(ModelMap model, HttpSession session) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        List<UserSchedule> userScheduleList = userScheduleService.getAllUserSchedules();
        model.addAttribute("userScheduleList", userScheduleList);
        model.addAttribute("batchList", batchList);
        model.addAttribute("data", new UserSchedule());
        return "/teacher/TCH-ATB-08";
    }

    @PostMapping("/searchUserScheduleList")
    public String searchUserScheduleList(@ModelAttribute("data") UserSchedule userSchedule, ModelMap model,
            HttpSession session) {
        Long batchId = userSchedule.getUser().getBatchList().get(0).getId();
        if (userSchedule.getDate() != null) {
            Schedule resSchedule = scheduleService.getScheduleByDate(userSchedule.getDate());
            if (resSchedule == null) {
                String teacherId = (String) session.getAttribute("uid");
                List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
                model.addAttribute("userScheduleList", new ArrayList<>());
                model.addAttribute("batchList", batchList);
                model.addAttribute("data", new UserSchedule());
                return "/teacher/TCH-ATB-08";

            }
            List<UserSchedule> list = userScheduleService.getUserScheduleListByBatchIdAndScheduleId(batchId,
                    resSchedule.getId());
            String teacherId = (String) session.getAttribute("uid");
            List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
            model.addAttribute("userScheduleList", list);
            model.addAttribute("batchList", batchList);
            model.addAttribute("data", new UserSchedule());
            return "/teacher/TCH-ATB-08";
        } else {
            List<UserSchedule> list = userScheduleService.getUserScheduleListByBatchIdOrScheduleId(batchId);
            String teacherId = (String) session.getAttribute("uid");
            List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
            model.addAttribute("userScheduleList", list);
            model.addAttribute("batchList", batchList);
            model.addAttribute("data", userSchedule);
            return "/teacher/TCH-ATB-08";
        }
    }

    @GetMapping("/postResource")
    public String setupPostResource(HttpSession session, ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-UPR-02";
    }

    //added by me
    @GetMapping("/assignment-table")
    public String setupAssignmentTable(HttpSession session, ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("batchList", batchList);
        model.addAttribute("assignmentList", assignmentService.getAllAssignment());
        return "/teacher/TCH-ASG-00";
    }

    @PostMapping("/createAssignment")
    public String createAssignment(@ModelAttribute("assignment") Assignment assignment, RedirectAttributes redirectAttrs)
            throws IllegalStateException, IOException {
        assignmentService.addAssignment(assignment);
        return "redirect:/teacher/assignment-table";
    }

    @GetMapping("/checkAssignment/{assignmentId}")
    public String checkAssignment(@PathVariable("assignmentId")Long assignmentId, ModelMap model){
        List<AssignmentAnswer> answerList = assignmentAnswerService.getAssignmentAnswerByAssignmentId(assignmentId);
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        String assignmentName = assignment.getName();
        model.addAttribute("answerList", answerList);
        model.addAttribute("assignmentName", assignmentName);
        return "/teacher/TCH-ASD-00";
    }

    @PostMapping("/assignmentFeedBack/{asgmAnswerId}")
    public String assignmentFeedBack(@PathVariable("asgmAnswerId") Long asgmAnswerId
    , HttpSession session
    , @RequestParam("comment")String comment
    , @RequestParam("mark")String score){

        String teacherId = (String) session.getAttribute("uid");
        AssignmentAnswer asgmAnswer = assignmentAnswerService.getAssignmentAnswerById(asgmAnswerId);
        String assignmentId = String.valueOf(asgmAnswer.getAssignment().getId());
        asgmAnswer.setComment(comment);
        asgmAnswer.setScore(score);
        asgmAnswer.setTeacherId(teacherId);
        assignmentAnswerService.addTeacherResponse(asgmAnswer);
        return "redirect:/teacher/checkAssignment/"+assignmentId;
    }

    @GetMapping("/downloadAnswer/{asgmAnswerId}")
    public String downloadAnswer(@PathVariable("asgmAnswerId") Long asgmAnswerId
    , HttpServletResponse response) throws IOException{
        AssignmentAnswer asgmAnswer = assignmentAnswerService.getAssignmentAnswerById(asgmAnswerId);
        String asgmName = asgmAnswer.getAssignment().getName();
        String fileName = asgmAnswer.getAnswerFile();
        String filePath = "C:\\AILP-V\\src\\main\\resources\\static\\courses\\"+asgmName+ "\\assignment"+ "\\answer\\"+fileName;
        File file = new File(filePath);
        response.setContentType("application/octet-stream");
        String headerKey = "Content Disposition";
        String headerValue = "Attachment File Name "+file.getName();
        response.setHeader(headerKey, headerValue);
        ServletOutputStream outputStream = response.getOutputStream();
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        byte[] buffer = new byte[8192];//8KB BUFFER
        int byteRead = -1;
        while((byteRead = inputStream.read(buffer)) != -1){
            outputStream.write(buffer,0,byteRead);
        }
        inputStream.close();
        outputStream.close();

        return "redirect:/teacher/assignmentFeedBack/"+asgmAnswerId;
    }

    //end

    @GetMapping("/postVideo")
    public String setupPostVideo(HttpSession session, ModelMap model) {
        String teacherId = (String) session.getAttribute("uid");
        List<Batch> batchList = userService.getTeacherBatchListById(teacherId);
        model.addAttribute("batchList", batchList);
        return "/teacher/TCH-UPV-03";
    }

}

package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    // Spring Boot
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    // Spring MVC
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // request
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        System.out.println(request.getParameter("code"));

        // response
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()){
            writer.write("<h1>NowCoder</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get

    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "20")int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }


    // Post
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    // http://localhost:8080/community/html/student.html
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success"; // http://localhost:8080/community/alpha/student
    }

    // Response HTML
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    // http://localhost:8080/community/alpha/teacher
    public ModelAndView getTeacher() { // return model and view
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "Jack");
        mav.addObject("age", 20);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) { // Model is from DispatcherServlet
        model.addAttribute("name", "UCSD");
        model.addAttribute("age", 80);
        return "/demo/view";
    }

    // Response JSON (async request) -> request from server without refreshing
    // Java objects -> JSON string -> JS Object
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody //return string, otherwise html
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Nick");
        emp.put("age", 23);
        emp.put("salary", 8000);
        return emp; // {"name":"Nick","salary":8000,"age":23}
    }

    // Multiple datapoints
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "Nick");
        emp.put("age", 23);
        emp.put("salary", 8000);
        list.add(emp);

        emp.put("name", "Ann");
        emp.put("age", 24);
        emp.put("salary", 8700);
        list.add(emp);

        emp.put("name", "Luna");
        emp.put("age", 20);
        emp.put("salary", 6000);
        list.add(emp);

        return list; // {"name":"Nick","salary":8000,"age":23}
    }
}


package com.tmindtech.api.example;

import com.tmindtech.api.base.annotation.AwesomeParam;
import com.tmindtech.api.base.exception.AwesomeException;
import com.tmindtech.api.example.model.Example;
import com.tmindtech.api.example.service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by RexQian on 2017/5/10.
 */
@RestController
@RequestMapping("/examples")
public class ExampleController {

    @Autowired
    ExampleService exampleService;

    @GetMapping
    public Object getList(
            @AwesomeParam(defaultValue = "0") int offset,
            @AwesomeParam(defaultValue = "100") int limit) {
        return exampleService.getList(offset, limit);
    }

    static class AddExample {
        public String name;
        public Boolean enabled;
    }

    @PostMapping
    public Object add(@RequestBody AddExample request) {
        return exampleService.add(request.name, request.enabled);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        exampleService.delete(id);
    }

    @GetMapping("/{id}")
    public Object getDetail(@PathVariable long id) {
        Example example = exampleService.get(id);
        if (example == null) {
            throw new AwesomeException(Config.ERROR_EXAMPLE_NOT_FOUND);
        }
        return example;
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable long id, @RequestBody AddExample request) {
        exampleService.update(id, request.name, request.enabled);
    }
}

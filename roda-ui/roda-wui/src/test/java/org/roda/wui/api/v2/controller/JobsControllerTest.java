package org.roda.wui.api.v2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.generics.select.SelectedItemsNoneRequest;
import org.roda.core.data.v2.jobs.CreateJobRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Reports;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.JobService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(JobsController.class)
public class JobsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @MockBean
    private IndexService indexService;

    // Autowire ObjectMapper for JSON conversions if needed for assertions
    // @WebMvcTest typically configures one.
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void contextLoads() throws Exception {
        // Basic test to ensure context loads and mockMvc is available
        assert mockMvc != null;
    }

    @Test
    public void testGetJobFromModel_Success() throws Exception {
        String jobId = "job123";
        // Use the Job.builder() as Job constructor is not public
        Job mockJob = Job.builder(jobId).name("Test Job").build();

        RequestContext mockRequestContext = new RequestContext();
        // Use User.builder() as User constructor is not public
        User mockUser = User.builder().id("userSuccess").username("testSuccess").build();
        mockRequestContext.setUser(mockUser);

        // Mock the static RequestUtils.parseHTTPRequest method
        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            // Mock the behavior of the jobService
            when(jobService.getJobFromModel(jobId)).thenReturn(mockJob);

            mockMvc.perform(get("/api/v2/jobs/{jobId}", jobId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(jobId))
                    .andExpect(jsonPath("$.name").value("Test Job"));
        }
    }

   @Test
   public void testGetJobFromModel_NotFound() throws Exception {
       String jobId = "job404";
       RequestContext mockRequestContext = new RequestContext();
       User mockUser = User.builder().id("userNotFound").username("testNotFound").build();
       mockRequestContext.setUser(mockUser);

       try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
           mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

           when(jobService.getJobFromModel(jobId)).thenThrow(new NotFoundException("Job not found"));

           mockMvc.perform(get("/api/v2/jobs/{jobId}", jobId)
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isNotFound());
       }
   }

   @Test
   public void testGetJobFromModel_ServiceThrowsUnauthorized() throws Exception {
       String jobId = "jobAuth";
       RequestContext mockRequestContext = new RequestContext();
       User mockUser = User.builder().id("userAuth").username("testAuth").build();
       mockRequestContext.setUser(mockUser);

       try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
           mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

           when(jobService.getJobFromModel(jobId)).thenThrow(new AuthorizationDeniedException("User not authorized"));

           mockMvc.perform(get("/api/v2/jobs/{jobId}", jobId)
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isUnauthorized()); // Assuming RESTException or Spring global exception handler maps this to 401
       }
   }

    @Test
    public void testCreateJob_Success() throws Exception {
        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setName("New Test Job");
        jobRequest.setPriority(JobPriority.NORMAL.name());
        jobRequest.setParallelism(JobParallelism.OFF.name());
        jobRequest.setPlugin("samplePlugin");
        jobRequest.setSourceObjects(new SelectedItemsNoneRequest());
        jobRequest.setSourceObjectsClass(null);
        jobRequest.setPluginParameters(null);

        Job createdJob = Job.builder("newJobId").name("New Test Job").build();

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userCreate").username("testCreate").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            doNothing().when(jobService).validateAndSetJobInformation(any(User.class), any(Job.class));
            when(jobService.createJob(any(Job.class), Mockito.eq(true))).thenReturn(createdJob);

            mockMvc.perform(post("/api/v2/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(jobRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("newJobId"))
                    .andExpect(jsonPath("$.name").value("New Test Job"));
        }
    }

    @Test
    public void testCreateJob_InvalidRequest() throws Exception {
        CreateJobRequest jobRequest = new CreateJobRequest();
        // jobRequest.setName(null); // Example of invalid setup

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userInvalid").username("testInvalid").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            doThrow(new RequestNotValidException("Invalid job data"))
                .when(jobService).validateAndSetJobInformation(any(User.class), any(Job.class));

            mockMvc.perform(post("/api/v2/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(jobRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    public void testCreateJob_ServiceThrowsJobAlreadyStartedException() throws Exception {
        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setName("Existing Job");
        jobRequest.setPriority(JobPriority.NORMAL.name());
        jobRequest.setParallelism(JobParallelism.OFF.name());
        jobRequest.setPlugin("samplePlugin");
        jobRequest.setSourceObjects(new SelectedItemsNoneRequest());
        jobRequest.setSourceObjectsClass(null);
        jobRequest.setPluginParameters(null);

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userJobStarted").username("testJobStarted").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            doNothing().when(jobService).validateAndSetJobInformation(any(User.class), any(Job.class));
            when(jobService.createJob(any(Job.class), Mockito.eq(true)))
                .thenThrow(new JobAlreadyStartedException("Job already started"));

            mockMvc.perform(post("/api/v2/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(jobRequest)))
                    .andExpect(status().isConflict());
        }
    }

    @Test
    public void testCreateJob_Unauthorized() throws Exception {
        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setName("Unauthorized Job");
        jobRequest.setPriority(JobPriority.NORMAL.name());
        jobRequest.setParallelism(JobParallelism.OFF.name());
        jobRequest.setPlugin("samplePlugin");
        jobRequest.setSourceObjects(new SelectedItemsNoneRequest());

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userUnauthorized").username("testUnauthorized").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            doThrow(new AuthorizationDeniedException("User not authorized to create job"))
                .when(jobService).validateAndSetJobInformation(any(User.class), any(Job.class));

            mockMvc.perform(post("/api/v2/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(jobRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    public void testStopJob_Success() throws Exception {
        String jobId = "jobToStop123";

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userStop").username("testStop").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            // Mock jobService.stopJob to do nothing (as it's a void method)
            doNothing().when(jobService).stopJob(jobId);

            mockMvc.perform(post("/api/v2/jobs/{jobId}/stop", jobId))
                    .andExpect(status().isOk()); // Void methods usually return 200 OK with empty body
        }
    }

    @Test
    public void testStopJob_NotFound() throws Exception {
        String jobId = "jobNotFound404";

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userStopNotFound").username("testStopNotFound").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            // Mock jobService.stopJob to throw NotFoundException
            doThrow(new NotFoundException("Job not found to stop")).when(jobService).stopJob(jobId);

            mockMvc.perform(post("/api/v2/jobs/{jobId}/stop", jobId))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    public void testStopJob_CannotBeStopped() throws Exception {
        String jobId = "jobCannotStop";

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userCannotStop").username("testCannotStop").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            // Mock jobService.stopJob to throw a GenericException (or a more specific one if applicable)
            // RequestNotValidException could be one if the state is not valid for stopping.
            doThrow(new RequestNotValidException("Job cannot be stopped in its current state"))
                .when(jobService).stopJob(jobId);

            mockMvc.perform(post("/api/v2/jobs/{jobId}/stop", jobId))
                    .andExpect(status().isBadRequest()); // Assuming RequestNotValidException maps to 400
        }
    }

    @Test
    public void testStopJob_Unauthorized() throws Exception {
        String jobId = "jobStopUnauthorized";

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userStopUnauthorized").username("testStopUnauthorized").build();
        // This user should ideally be configured to fail ControllerAssistant.checkRoles
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            // Simulate AuthorizationDeniedException from the service layer
            // (or could be from ControllerAssistant.checkRoles)
            doThrow(new AuthorizationDeniedException("User not authorized to stop job"))
                .when(jobService).stopJob(jobId);

            mockMvc.perform(post("/api/v2/jobs/{jobId}/stop", jobId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    public void testListJobReports_Success() throws Exception {
        String jobId = "jobWithReports123";
        Reports mockReports = new Reports();
        Report sampleReport = Report.builder().id("report1").message("Sample report content").build();
        mockReports.setReports(Collections.singletonList(sampleReport));
        mockReports.setTotal(1); // Changed from 1L

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userReports").username("testReports").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            when(jobService.getJobReportsFromIndexResult(
                any(User.class),
                Mockito.eq(jobId),
                Mockito.eq(false),
                Mockito.eq("0"),
                Mockito.eq("10"),
                Mockito.anyList()
            )).thenReturn(mockReports);

            mockMvc.perform(get("/api/v2/jobs/reports/{jobId}", jobId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.total").value(1)) // Expecting number 1
                    .andExpect(jsonPath("$.reports[0].id").value("report1"));
        }
    }

    @Test
    public void testListJobReports_WithParams_Success() throws Exception {
        String jobId = "jobWithReportsParams123";
        boolean justFailed = true;
        String start = "5";
        String limit = "20";
        Reports mockReports = new Reports();
        Report r = Report.builder().id("r2").build();
        mockReports.setReports(Collections.singletonList(r));
        mockReports.setTotal(1); // Changed from 1L


        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userReportsParams").username("testReportsParams").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            when(jobService.getJobReportsFromIndexResult(
                any(User.class),
                Mockito.eq(jobId),
                Mockito.eq(justFailed),
                Mockito.eq(start),
                Mockito.eq(limit),
                Mockito.anyList()
            )).thenReturn(mockReports);

            mockMvc.perform(get("/api/v2/jobs/reports/{jobId}", jobId)
                    .param("justFailed", String.valueOf(justFailed))
                    .param("start", start)
                    .param("limit", limit))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.total").value(1)) // Expecting number 1
                    .andExpect(jsonPath("$.reports[0].id").value("r2"));
        }
    }

    @Test
    public void testListJobReports_Unauthorized() throws Exception {
        String jobId = "jobReportsUnauthorized";

        RequestContext mockRequestContext = new RequestContext();
        User mockUser = User.builder().id("userReportsUnauth").username("testReportsUnauth").build();
        mockRequestContext.setUser(mockUser);

        try (MockedStatic<RequestUtils> mockedRequestUtils = Mockito.mockStatic(RequestUtils.class)) {
            mockedRequestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(mockRequestContext);

            when(jobService.getJobReportsFromIndexResult(
                any(User.class),
                Mockito.eq(jobId),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList()
            )).thenThrow(new AuthorizationDeniedException("User not authorized to list job reports"));

            mockMvc.perform(get("/api/v2/jobs/reports/{jobId}", jobId))
                    .andExpect(status().isUnauthorized());
        }
    }
}

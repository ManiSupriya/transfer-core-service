package com.mashreq.transfercoreservice.cache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class MobRedisServiceTest {
	@InjectMocks
	private MobRedisService service;
	@Mock
	private RedisTemplate<String, Object> mobRedisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SetOperations<String, Object> setOperations;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mobRedisTemplate.opsForSet()).thenReturn(setOperations);
        Mockito.when(setOperations.remove(Mockito.anyString(), Mockito.anyString())).thenReturn(1l);
        ReflectionTestUtils.setField(service, "durationInMinutes", 30l);
    }
    
	@Test
	public void test_removeValueFromSet() {
		Mockito.when(mobRedisTemplate.opsForSet()).thenReturn(setOperations);
		service.removeValueFromSet("key", "value");
		Mockito.verify(setOperations,Mockito.times(1)).remove("key", "value");
	}

}

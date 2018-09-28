package org.mosip.registration.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mosip.kernel.core.spi.logging.MosipLogger;
import org.mosip.kernel.logger.appenders.MosipRollingFileAppender;
import org.mosip.registration.config.AuditFactory;
import org.mosip.registration.constants.AppModuleEnum;
import org.mosip.registration.constants.AuditEventEnum;
import org.mosip.registration.constants.RegProcessorExceptionEnum;
import org.mosip.registration.dto.RegistrationDTO;
import org.mosip.registration.dto.ResponseDTO;
import org.mosip.registration.exception.RegBaseCheckedException;
import org.mosip.registration.exception.RegBaseUncheckedException;
import org.mosip.registration.service.packet.PacketHandlerService;
import org.mosip.registration.service.PacketCreationService;
import org.mosip.registration.service.PacketEncryptionService;
import org.springframework.test.util.ReflectionTestUtils;

public class PacketHandlerAPITest {
	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	private PacketHandlerService packetHandlerService;
	@Mock
	private PacketCreationService packetCreationManager;
	@Mock
	private PacketEncryptionService packetEncryptionManager;
	@Mock
	private AuditFactory auditFactory;
	@Mock
	private MosipRollingFileAppender mosipRollingFileAppender;
	@Mock
	private MosipLogger logger;
	ResponseDTO mockedSuccessResponse;

	@Before
	public void initialize() {
		mockedSuccessResponse = new ResponseDTO();
		//mockedSuccessResponse.setCode("0000");
		//mockedSuccessResponse.setMessage("Success");
		Mockito.doNothing().when(auditFactory).audit(Mockito.any(AuditEventEnum.class),
				Mockito.any(AppModuleEnum.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void testHandle() throws RegBaseCheckedException {
		ReflectionTestUtils.setField(packetHandlerService, "LOGGER", logger);
		
		Mockito.when(packetCreationManager.create(Mockito.any(RegistrationDTO.class))).thenReturn("Packet Creation".getBytes());
		Mockito.when(
				packetEncryptionManager.encrypt(Mockito.any(RegistrationDTO.class), Mockito.anyString().getBytes()))
				.thenReturn(mockedSuccessResponse);
		Assert.assertSame(mockedSuccessResponse, packetHandlerService.handle(new RegistrationDTO()));
	}

	@Test
	public void testCreationException() throws RegBaseCheckedException {
		ReflectionTestUtils.setField(packetHandlerService, "LOGGER", logger);

		Mockito.when(packetCreationManager.create(Mockito.any(RegistrationDTO.class))).thenReturn(null);
		ResponseDTO actualResponse = packetHandlerService.handle(new RegistrationDTO());
		Assert.assertEquals(RegProcessorExceptionEnum.REG_PACKET_CREATION_ERROR_CODE.getErrorCode(),
				actualResponse.getErrorResponseDTOs().get(0).getCode());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = NullPointerException.class)
	public void testHandlerException() throws RegBaseCheckedException {
		ReflectionTestUtils.setField(packetHandlerService, "LOGGER", logger);

		Mockito.when(packetCreationManager.create(Mockito.any(RegistrationDTO.class)))
				.thenThrow(RegBaseUncheckedException.class);
		Mockito.when(
				packetEncryptionManager.encrypt(Mockito.any(RegistrationDTO.class), Mockito.anyString().getBytes()))
				.thenReturn(mockedSuccessResponse);
		packetHandlerService.handle(new RegistrationDTO());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = NullPointerException.class)
	public void testHandlerChkException() throws RegBaseCheckedException {
		ReflectionTestUtils.setField(packetHandlerService, "LOGGER", logger);

		Mockito.when(packetCreationManager.create(Mockito.any(RegistrationDTO.class)))
				.thenThrow(RegBaseCheckedException.class);
		Mockito.when(
				packetEncryptionManager.encrypt(Mockito.any(RegistrationDTO.class), Mockito.anyString().getBytes()))
				.thenReturn(mockedSuccessResponse);
		packetHandlerService.handle(new RegistrationDTO());
	}

}

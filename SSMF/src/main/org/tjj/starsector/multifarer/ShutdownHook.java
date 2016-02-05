package org.tjj.starsector.multifarer;

import org.fourthline.cling.UpnpService;

public class ShutdownHook implements Runnable {

	private UpnpService upnpService;
	
	public ShutdownHook(UpnpService upnpService) {
		this.upnpService = upnpService;
	}
	
	@Override
	public void run() {
		upnpService.shutdown();
		System.out.println("Cleanly shutdown");
	}

}

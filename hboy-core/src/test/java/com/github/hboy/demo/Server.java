package com.github.hboy.demo;

import com.github.hboy.center.ServiceExporter;
import com.github.hboy.common.config.CenterConfig;
import com.github.hboy.common.config.ExportConfig;

public class Server {
	public static void main(String[] args) {
		 
		String host = "127.0.0.1";
		Server server = new Server();
		server.startServer(host);
		
		synchronized (Server.class) {
            while (true) {
                try {
                	Server.class.wait();
                } catch (InterruptedException e) {
                }
            }
        }
		
		
	}
	
		public void startServer(String host) {
				CenterConfig centerConfig = new CenterConfig(
						"172.27.137.12:2181");
				centerConfig.setApplication("test1");
				ExportConfig exportConfig = new ExportConfig();
				exportConfig.setHost(host);
				exportConfig.setPort(8186);
				
				ServiceExporter<Scribe> exporter = new ServiceExporter<Scribe>();
				exporter.setCenterConfig(centerConfig);
				exporter.setExportConfig(exportConfig);
				exporter.setService(new SwiftScribe());
				exporter.setInterface(Scribe.class);
				exporter.export();
			 
		}
}


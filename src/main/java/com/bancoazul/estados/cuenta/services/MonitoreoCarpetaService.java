package com.bancoazul.estados.cuenta.services;

/**
 * Class to handle CREATE_EVENT changes in specific file system folders
 * 
 * @author Melvin Reyes
 */
public interface MonitoreoCarpetaService {

	/**
	 * Watch CREATE_EVENT changes
	 */
	public void watchDirectory();

}

package net.fghijk.maven.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class MojoUtils {
	private MojoUtils() {
	}

	public static void executeProcess(ProcessBuilder processBuilder, Log log) throws MojoExecutionException {
		try {
			Process process = processBuilder.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					log.info(line);
				}
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					log.info(line);
				}
			}

			try {
				process.waitFor();
			} catch (InterruptedException e) {
				throw new MojoExecutionException("Could not finish process", e);
			}

			int exitValue = process.exitValue();

			if (exitValue != 0) {
				throw new MojoExecutionException("Process returned " + exitValue);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Could not start process", e);
		}
	}
}
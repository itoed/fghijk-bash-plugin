package net.fghijk.maven.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "run")
public class RunMojo extends AbstractMojo {
	@Parameter(required = true)
	private String script;

	@Parameter
	private boolean skipRun;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();

		if (skipRun) {
			log.info("Skipping fghijk-bash-plugin:run");
			return;
		}

		script = script.trim();

		if (script == null || script.isEmpty()) {
			log.error("Missing argument: script");
			throw new MojoFailureException("Missing argument: script");
		}

		if (System.getProperty("os.name").toUpperCase(Locale.ENGLISH).startsWith("WINDOWS")) {
			log.error("Operating system not supported: " + System.getProperty("os.name"));
		}

		boolean isDebugEnabled = log.isDebugEnabled();

		log.info("Executing bash script" + (isDebugEnabled ? " in debug mode" : ""));

		File scriptFile;

		try {
			scriptFile = File.createTempFile("fghijk-bash-plugin", ".sh.tmp");

			try (PrintWriter writer = new PrintWriter(scriptFile)) {
				writer.print(scriptFile);
			}

			if (log.isInfoEnabled()) {
				try (BufferedReader in = new BufferedReader(new FileReader(scriptFile))) {
					String line;
					while ((line = in.readLine()) != null) {
						log.info(">>>   " + line);
					}
				}
			}

		} catch (IOException e) {
			throw new MojoExecutionException("Error creating script file", e);
		}

		List<String> processArgs = new ArrayList<>();
		processArgs.add("bash");
		if (isDebugEnabled) {
			processArgs.add("-x");
		}
		processArgs.add(scriptFile.getAbsolutePath());

		ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
		processBuilder.redirectErrorStream(true);
		
		MojoUtils.executeProcess(processBuilder, log);
	}
}
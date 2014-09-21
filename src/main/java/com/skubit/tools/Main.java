/**
 * Copyright 2014 Skubit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.skubit.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Hex;

public class Main {

	@SuppressWarnings("static-access")
	private static Options addOptions() {
		// options: post, get, put, delete
		// options: apiSecret, url, noance, location of body

		Options options = new Options();
		Option apiSecretKeyOption = OptionBuilder
				.withLongOpt("apiSecretKey").hasArg()
				.withDescription(
						"API secret key for the application (obtained from website)")
				.create("secret");

		Option apiKeyOption = OptionBuilder
				.withLongOpt("apiKey").hasArg()
				.withDescription(
						"API client key for the application (obtained from website)")
				.create("key");

		Option methodOption = OptionBuilder.withLongOpt("method").hasArg()
				.withDescription("HTTP Method").create("m");

		Option noanceOption = OptionBuilder.withLongOpt("nonce").hasArg()
				.withDescription("Nonce field").create("n");

		Option bodyOption = OptionBuilder.withLongOpt("body").hasArg()
				.withDescription("File location of body element").create("b");

		Option helpOption = OptionBuilder.withLongOpt("help")
				.withDescription("Display help").create("h");

		return options.addOption(helpOption).addOption(apiSecretKeyOption)
				.addOption(bodyOption).addOption(noanceOption)
				.addOption(methodOption).addOption(apiKeyOption);
	}

	// get, post, put
	public static String calculateSignature(byte[] apiSecretKey, String uri,
			String noance, String body) throws NoSuchAlgorithmException,
			InvalidKeyException {
		StringBuilder message = new StringBuilder();
		message.append(noance).append(uri);
		if (body != null) {
			message.append(body);
		}
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(apiSecretKey, "HmacSHA256"));
		return new String(Hex.encodeHex(mac.doFinal(message.toString()
				.getBytes())));
	}

	public static String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static void exit(String message) {
		System.out.println("\r\n" + message + "\r\n");
		printHelp();
		System.exit(-1);
	}

	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;

		try {
			commandLine = parser.parse(addOptions(), args);
		} catch (ParseException ex) {
			exit("Invalid command arguments");
		}

		if (commandLine.hasOption("h")) {
			printHelp();
			System.exit(0);
		}

		String method = commandLine.getOptionValue("m");
		if (method == null) {
			method = "GET";
		}

		String nonce = commandLine.getOptionValue("n");
		if (nonce == null) {
			exit("Must specify nonce on the command line");
		}

		String apiSecretKey = commandLine.getOptionValue("secret");
		if (apiSecretKey == null) {
			exit("Must specify apiSecretKey on the command line");
		}

		String apiKey = commandLine.getOptionValue("key");
		if (apiKey == null) {
			exit("Must specify apiKey on the command line");
		}

		String bodyFile = commandLine.getOptionValue("b");

		String body = null;
		if (bodyFile != null) {
			try {
				body = convertStreamToString(new FileInputStream(bodyFile));
			} catch (FileNotFoundException e) {
				exit(e.getMessage());
			}
		}
		String[] as = commandLine.getArgs();
		if (as.length != 1) {
			exit("Must specify url");
		}

		String uri = as[0];

		String signature = null;
		try {
			signature = calculateSignature(apiSecretKey.getBytes(), uri, nonce,
					body);
		} catch (InvalidKeyException e) {
			exit(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			exit(e.getMessage());
		}

		try {
			makeRequest(uri, method, apiKey, nonce, signature, body);
		} catch (IOException e) {
			exit(e.getMessage());
		}
	}

	public static void makeRequest(String url, String method, String apiKey,
			String noance, String signature, String body) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url)
				.openConnection();

		connection.setRequestMethod(method);

		connection.setRequestProperty("skubit-access-key", apiKey);
		connection.setRequestProperty("skubit-access-nonce", noance);
		connection.setRequestProperty("skubit-access-signature", signature);

		if (method.toLowerCase().equals("post")
				|| (method.toLowerCase().equals("put")) && body != null) {
			connection.setDoOutput(true);

			connection.setRequestProperty("Content-Type", "application/json");
			OutputStreamWriter osw = new OutputStreamWriter(
					connection.getOutputStream());
			osw.write(body);
			osw.close();
		}
		//connection.setDoInput(true);
		
		int responseCode = connection.getResponseCode();
		System.out.println(responseCode);
	//	
		InputStream is = connection.getInputStream();
		byte[] buffer = new byte[8192];
		while (is.read(buffer) != -1) {
			System.out.print(new String(buffer));
		}
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("skubit: url", addOptions());
	}
}

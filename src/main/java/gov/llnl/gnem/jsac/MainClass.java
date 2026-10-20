/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.jsac;

import java.awt.HeadlessException;
import java.util.ServiceLoader;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.commands.SacCommandParser;
import gov.llnl.gnem.jsac.plots.plot.PlotFrame;
import llnl.gnem.core.util.ExceptionDialog;
import llnl.gnem.core.util.FileUtil.DriveMapper;
import org.apache.commons.cli.HelpFormatter;
import java.io.InputStream;

import java.util.Properties;
import java.util.logging.Level;
import llnl.gnem.core.util.ApplicationLogger;

/**
 *
 * @author dodge1
 */
public class MainClass {

    private static final Logger log = LoggerFactory.getLogger(MainClass.class);

    private static final ServiceLoader<CommandParser> additionalParsers = ServiceLoader.load(CommandParser.class);

    private static Interpreter interpreter;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void main(String[] args) {

        displayVersionAndDate();
        getCommandLineInfo(args);
        DriveMapper.setupWindowsNFSDriveMap();

        SwingUtilities.invokeLater(() -> {
            try {
                PlotFrame.getInstance().setVisible(false);
            }
            catch (ExceptionInInitializerError | HeadlessException he) {
                log.trace("Headless environment detected.");
            }
            catch (Exception e) {
                ExceptionDialog.displayError(e);
            }
        });
        try {
            SacCommandParser.getInstance().initialize();
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
            System.exit(1);
        }
        interpreter = new Interpreter();
        Thread thread = new Thread(interpreter);
        thread.start();

    }

    public static void displayVersionAndDate() {
        Properties prop = new Properties();
        try (InputStream input = MainClass.class.getClassLoader().getResourceAsStream("build.properties")) {
            if (input == null) {
                System.out.println("Unable to find build.properties");
            }
            prop.load(input);
            System.out.println("Version: " + prop.getProperty("build.version"));
            System.out.println("Build Date: " + prop.getProperty("build.date"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printUsage(Options options) {
        String usage = getUsageString();
        String footer = "";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(usage, "", options, footer, false);

    }

    private static void getCommandLineInfo(String[] args) {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Show general help message...");
        Option logLevelOption = new Option("L", "LogLevel", true, "The logging level to use.");

        DefaultParser baseParser = new DefaultParser();
        options.addOption(help);
        options.addOption(logLevelOption);
        try {
            CommandLine cmd = baseParser.parse(options, args);
            if (cmd.hasOption(help.getOpt())) {
                printUsage(options);
                System.exit(0);
            }
            if (cmd.hasOption(logLevelOption.getOpt())) {
                String level = cmd.getOptionValue(logLevelOption.getOpt());
                Level logLevel = Level.parse(level.toUpperCase());
                 
                ApplicationLogger.getInstance().setLevel(logLevel);
            }
            additionalParsers.forEach(f -> f.apply(options, args));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
            System.exit(2);
        }
    }

    private static String getUsageString() {
        if (additionalParsers.iterator().hasNext()) {
            return additionalParsers.iterator().next().getHelpText();
        } else {
            return "jsac [options]";
        }
    }

    public static Interpreter getInterpreter() {
        return interpreter;
    }

}

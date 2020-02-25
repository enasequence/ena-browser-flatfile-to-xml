package uk.ac.ebi.ena.browser.flatfiletoxml;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblEntryReader;
import uk.ac.ebi.embl.flatfile.writer.xml.XmlEntryWriter;

import javax.validation.constraints.Pattern;
import java.io.*;
import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class FlatfileToXmlApplication implements CommandLineRunner {

    @Value("${flatfile}")
    String flatfile;

    @Value("${xmlfile}")
    String xmlfile;

    @Pattern(regexp = "(CDS|EMBL|MASTER|NCR|)")
    @Value("${format:#{null}}")
    String inputFormat;

    private static final String DELIMITER = "//\n";

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FlatfileToXmlApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Converting file {}", flatfile);
        validateArguments();

        File file = new File(flatfile);
        File outputFile = new File(xmlfile);
        EmblEntryReader.Format format = EmblEntryReader.Format.EMBL_FORMAT;
        if (StringUtils.isNotBlank(inputFormat)) {
            format = EmblEntryReader.Format.valueOf(inputFormat + "_FORMAT");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator());
            writer.write("<ROOT>" + System.lineSeparator());
            try (Scanner scanner = new Scanner(file)) {
                scanner.useDelimiter(DELIMITER);
                while (scanner.hasNext()) {
                    String s = scanner.next();
                    if (null != s && !s.equals("")) {
                        s = s + DELIMITER;
                        try (BufferedReader stringReader = new BufferedReader(new StringReader(s))) {
                            EmblEntryReader reader = new EmblEntryReader(stringReader, format, null);
                            reader.read();
                            Entry entry = reader.getEntry();
                            new XmlEntryWriter(entry).write(writer);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                String message = "Failed attempting to convert large entry. This tool only supports converting an " +
                        "entry up to 2GB in size. i.e. your input file may be larger than 2GB if it contains multiple" +
                        " records, but each individual entry should not exceed 2GB in size.";
                System.err.println(message);
            }
            writer.write("</ROOT>");
            System.out.println("Conversion complete.");
        }
    }

    private void validateArguments() {
        String valid = null;
        if (StringUtils.isBlank(flatfile) || !new File(flatfile).exists()) {
            valid = "Please specify full path of flatfile.";
        }
        File xmlfile = new File(this.xmlfile);
        if (StringUtils.isBlank(this.xmlfile) || !xmlfile.getParentFile().exists() || !xmlfile.getParentFile().canWrite()) {
            valid = ("Please specify full path of xml output file.");
        }
        if (StringUtils.isBlank(inputFormat)) {
            System.out.println("Flatfile format not specified. Defaulting to EMBL format.");
        }
        if (StringUtils.isNotBlank(valid)) {
            System.out.println("Unable to process:" + valid);
            System.out.println("Usage: <flatfile path> <xml output path> <flatfile format: EMBL/CDS/NCR/MASTER>");
            System.out.println("Last argument is optional. Default format is EMBL.");
            System.out.println("e.g.");
            System.out.println("./ff-to-xml.sh /home/user/ABC.txt /home/user/ABC.xml CDS");
            System.out.println("ff-to-xml.bat c:\\user\\ABC.txt c:\\user\\ABC.xml");
            System.exit(1);
        }
    }
}

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
import java.util.Arrays;

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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
             BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator());
            writer.write("<ROOT>" + System.lineSeparator());

            EmblEntryReader inputReader = new EmblEntryReader(bufferedReader,
                    EmblEntryReader.Format.EMBL_FORMAT, file.getName());
            inputReader.read();

            while (inputReader.isEntry()) {
                // Get the next EMBL Entry
                Entry entry = inputReader.getEntry();
                new XmlEntryWriter(entry).write(writer);
                writer.flush();
                inputReader.read();
            }
            writer.write("</ROOT>");
            System.out.println("Conversion complete.");
        } catch (Exception e) {
            log.error("Conversion failed:", e);
            log.error("Please open a ticket at https://www.ebi.ac.uk/ena/browser/support with the full output above.");
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

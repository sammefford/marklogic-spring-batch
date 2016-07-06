package com.marklogic.spring.batch.config;

import com.marklogic.spring.batch.item.DocumentItemWriter;
import com.marklogic.spring.batch.item.file.MlcpFileReader;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

/**
 * TODO Is this needed when we have ImportDocumentsFromDirectoryConfig? This seemingly should at least
 * be renamed to capture its scope, which is to use Apache Tika to process documents.
 */
public class LoadDocumentsFromDirectoryConfig extends AbstractMarkLogicBatchConfig {

    @Bean
    public Job loadDocumentsFromDirectoryJob(@Qualifier("loadDocumentsFromDirectoryJobStep1") Step step) {
        return jobBuilderFactory.get("loadImagesFromDirectoryJob").start(step).build();
    }

    @Bean
    @JobScope
    public Step loadDocumentsFromDirectoryJobStep1(
            @Value("#{jobParameters['input_file_path']}") String inputFilePath,
            @Value("#{jobParameters['input_file_pattern']}") String inputFilePattern) {

        ItemProcessor<Resource, Document> processor = new ItemProcessor<Resource, Document>() {
            @Override
            public Document process(Resource item) throws Exception {
                ContentHandler handler = new ToXMLContentHandler();
                AutoDetectParser parser = new AutoDetectParser();
                Metadata metadata = new Metadata();
                parser.parse(item.getInputStream(), handler, metadata);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(handler.toString())));
                document.setDocumentURI(item.getFilename());
                return document;
            }
        };

        return stepBuilderFactory.get("step1")
                .<Resource, Document>chunk(getChunkSize())
                .reader(new MlcpFileReader(inputFilePath, inputFilePattern))
                .processor(processor)
                .writer(new DocumentItemWriter(getDatabaseClient()))
                .build();
    }
}

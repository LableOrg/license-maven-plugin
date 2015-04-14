/**
 * Copyright (C) 2008 Mycila (mathieu.carbou@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycila.maven.plugin.license;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.mycila.maven.plugin.license.util.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class UpdateMojoTest {
    public static final String LS = System.getProperty("line.separator");
    
    @Test
    public void test_update() throws Exception {
        File tmp = new File("target/test/update");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/doc1.txt"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/doc2.txt"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/header.txt";
        updater.project = new MavenProjectStub();
        updater.properties = ImmutableMap.of("year", "2008");
        updater.execute();

        assertEquals(FileUtils.read(new File(tmp, "doc1.txt"), System.getProperty("file.encoding")), "====\r\n    My @Copyright license 2 with my-custom-value and 2008 and doc1.txt\r\n====\r\n\r\nsome data\r\n");
        assertEquals(FileUtils.read(new File(tmp, "doc2.txt"), System.getProperty("file.encoding")), "====\r\n    My @Copyright license 2 with my-custom-value and 2008 and doc2.txt\r\n====\r\n\r\nsome data\r\n");
    }

    @Test
    public void test_skipExistingHeaders() throws Exception {
        File tmp = new File("target/test/test_skipExistingHeaders");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/doc1.txt"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/doc2.txt"), tmp);

        // only update those files without a copyright header
        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/header.txt";
        updater.project = new MavenProjectStub();
        updater.properties = ImmutableMap.of("year", "2008");
        updater.skipExistingHeaders = true;
        updater.execute();

        assertEquals(FileUtils.read(new File(tmp, "doc1.txt"), System.getProperty("file.encoding")), "====\r\n    My @Copyright license 2 with my-custom-value and 2008 and doc1.txt\r\n====\r\n\r\nsome data\r\n");
        assertEquals(FileUtils.read(new File(tmp, "doc2.txt"), System.getProperty("file.encoding")), "====\r\n    Copyright license\r\n====\r\n\r\nsome data\r\n");

        // expect unchanged header to fail check against new header
        LicenseCheckMojo check = new LicenseCheckMojo();
        check.basedir = tmp;
        check.header = "src/test/resources/update/header.txt";
        check.project = new MavenProjectStub();
        check.properties = ImmutableMap.of("year", "2008");
        check.skipExistingHeaders = false;

        try {
            check.execute();
            fail();
        } catch (MojoExecutionException e) {
            assertEquals("Some files do not have the expected license header", e.getMessage());
            assertEquals(1, check.missingHeaders.size());
        }

        // check again ignoring unchanged headers, should not fail
        check.skipExistingHeaders = true;
        check.execute();
    }

    @Test
    public void test_issue50() throws Exception {
        File tmp = new File("target/test/update/issue50");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue50/test1.properties"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue50/test2.properties"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue50/test3.properties"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/header.txt";
        updater.properties = ImmutableMap.of("year", "2008");
        updater.mapping = new HashMap<String, String>() {{
            put("properties", "SCRIPT_STYLE");
        }};
        updater.project = new MavenProjectStub();
        updater.execute();

        String test1 = FileUtils.read(new File(tmp, "test1.properties"), System.getProperty("file.encoding")).replaceAll("\\n", LS);
        String test2 = FileUtils.read(new File(tmp, "test2.properties"), System.getProperty("file.encoding"));
        String test3 = FileUtils.read(new File(tmp, "test3.properties"), System.getProperty("file.encoding"));

        assertEquals(test1, test2.replace("test2.properties", "test1.properties"));
        assertEquals(test1, test3.replace("test3.properties", "test1.properties"));
    }

    @Test
    public void test_issue48() throws Exception {
        File tmp = new File("target/test/update/issue48");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue48/test1.php"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue48/test2.php"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/header.txt";
        updater.properties = ImmutableMap.of("year", "2008");
        updater.mapping = new HashMap<String, String>() {{
            put("properties", "SCRIPT_STYLE");
        }};
        updater.project = new MavenProjectStub();
        updater.execute();

        assertEquals(FileUtils.read(new File(tmp, "test1.php"), System.getProperty("file.encoding")), "\r\n" +
            "\r\n" +
            "<?php\r\n" +
            "/*\r\n" +
            " * My @Copyright license 2 with my-custom-value and 2008 and test1.php\r\n" +
            " */\r\n" +
            "\r\n" +
            "class Conference extends Service {}\r\n" +
            "\r\n" +
            "?>\r\n");
        assertEquals(FileUtils.read(new File(tmp, "test2.php"), System.getProperty("file.encoding")), "\r\n" +
            "\r\n" +
            "<?php\r\n" +
            "/*\r\n" +
            " * My @Copyright license 2 with my-custom-value and 2008 and test2.php\r\n" +
            " */\r\n" +
            "\r\n" +
            "class Conference extends Service {}\r\n" +
            "\r\n" +
            "?>\r\n");
    }

    @Test
    public void test_issue44() throws Exception {
        File tmp = new File("target/test/update/issue44");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue44/issue44-3.rb"), tmp);
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue44/test.asp"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/header.txt";
        updater.properties = ImmutableMap.of("year", "2008");
        updater.project = new MavenProjectStub();
        updater.execute();

        assertEquals(FileUtils.read(new File(tmp, "issue44-3.rb"), System.getProperty("file.encoding")), "#" + LS + "" +
            "# My @Copyright license 2 with my-custom-value and 2008 and issue44-3.rb" + LS + "" +
            "#" + LS + "" +
            "" + LS + "" +
            "# code comment" + LS + "" +
            "ruby code here" + LS + "");

        assertEquals(FileUtils.read(new File(tmp, "test.asp"), System.getProperty("file.encoding")), "<%\n" +
            "    My @Copyright license 2 with my-custom-value and 2008 and test.asp\n" +
            "%>" +
            "\n" +
            "asp code");
    }

    @Test
    public void test_issue_14() throws Exception {
        File tmp = new File("target/test/update/issue14");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/update/issue14/test.properties"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/update/issue14/header.txt";
        updater.project = new MavenProjectStub();
        updater.execute();
        final String expectedString = "#" + LS + "" +
                "# Copyright (C) 2013 Salzburg Research." + LS + "" +
                "#" + LS + "" +
                "# Licensed under the Apache License, Version 2.0 (the \"License\");" + LS + "" +
                "# you may not use this file except in compliance with the License." + LS + "" +
                "# You may obtain a copy of the License at" + LS + "" +
                "#" + LS + "" +
                "#         http://www.apache.org/licenses/LICENSE-2.0" + LS + "" +
                "#" + LS + "" +
                "# Unless required by applicable law or agreed to in writing, software" + LS + "" +
                "# distributed under the License is distributed on an \"AS IS\" BASIS," + LS + "" +
                "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied." + LS + "" +
                "# See the License for the specific language governing permissions and" + LS + "" +
                "# limitations under the License." + LS + "" +
                "#" + LS + "" +
                "" + LS + "" +
                "meta.tables            = SHOW TABLES;" + LS + "" +
                "meta.version           = SELECT mvalue FROM metadata WHERE mkey = 'version';" + LS + "" +
                "" + LS + "" +
                "# get sequence numbers" + LS + "" +
                "seq.nodes              = SELECT nextval('seq_nodes')" + LS + "" +
                "seq.triples            = SELECT nextval('seq_triples')" + LS + "" +
                "seq.namespaces         = SELECT nextval('seq_namespaces')" + LS + "";
        final String readModifiedContent = FileUtils.read(new File(tmp, "test.properties"), System.getProperty("file.encoding"));

        assertEquals(expectedString, readModifiedContent);
    }
    
    @Test
    public void test_issue71_canSkipSeveralLines() throws Exception {
        File tmp = new File("target/test/update/issue71");
        tmp.mkdirs();
        FileUtils.copyFileToFolder(new File("src/test/resources/issues/issue-71/issue-71.txt.extended"), tmp);

        LicenseFormatMojo updater = new LicenseFormatMojo();
        updater.basedir = tmp;
        updater.header = "src/test/resources/issues/issue-71/issue-71-header.txt";
        updater.project = new MavenProjectStub();
        updater.mapping = new HashMap<String, String>() {{
            put("txt.extended", "EXTENDED_STYLE");
        }};
        updater.headerDefinitions = new String[]{"/issues/issue-71/issue-71-additionalHeaderDefinitions.xml"};
        updater.execute();
        
        
        // Check that all the skipable header has been correctly skipped
        List<String> linesOfModifiedFile = Files.readLines(new File(tmp, "issue-71.txt.extended"), Charset.defaultCharset());
        assertThat(linesOfModifiedFile.get(0 /* line 1 */), is("|||"));
        assertThat(linesOfModifiedFile.get(8) /* line 9 */, is("|||"));
    }
}

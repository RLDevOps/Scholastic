/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.util.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.test.OlatcoreTestCaseWithMocking;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.devtools.TranslationDevManager;
import org.olat.core.util.i18n.ui.InlineTranslationInterceptHandlerController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Testing i18n Translation Tool and Translation Developers Tools
 * <P>
 * Initial Date: 01.09.2008 <br>
 * 
 * @author gnaegi
 * @author rhaag
 */

public class I18nTest extends OlatcoreTestCaseWithMocking {

	private OLog log = Tracing.createLoggerFor(I18nTest.class);

	@Autowired
	private I18nManager i18nMgr;
	@Autowired
	private TranslationDevManager tDMgr;
	private static final String testSourceBundle = "org.olat.core.util.i18n.junittestdata.devtools.source";
	private static final String testTargetBundle = "org.olat.core.util.i18n.junittestdata.devtools.target";
	private static final String testMoveTargetBundle = "org.olat.core.util.i18n.junittestdata.movetarget.source";

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup() throws Exception {
		// see prepareDevToolTests() !
		Settings.setJUnitTest(true);
	}

	@After
	public void tearDown() throws Exception {
		String testNewBundle = "org.olat.core.util.i18n.junittestdata.new";
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		File baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testNewBundle);
		// only delete files when basedir available
		if (baseDir == null) return;
		File testFile = i18nMgr.getPropertiesFile(testLocale, testNewBundle, baseDir);
		// delete not only new/_i18n/LocalStrings_de.properties, delete also _i18n and new itself
		if (testFile.getParentFile().getParentFile().exists()) {
			FileUtils.deleteDirsAndFiles(testFile.getParentFile().getParentFile(), true, true);
		}
		// don't do inline translation markup
		i18nMgr.setMarkLocalizedStringsEnabled(null, false);
		// cleanup dev tools test case files
		removeDevToolTests();
	}

	protected void prepareDevToolTests() {
		// create test fixure in devtools.source
		// Roman: you can't check in a 'default' package (illegal package name in java)
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		// key 1
		String key = "key.to.move";
		I18nItem i18nItem = i18nMgr.getI18nItem(testSourceBundle, key, testLocale);
		i18nMgr.saveOrUpdateI18nItem(i18nItem, "I have to go");
		i18nMgr.setAnnotation(i18nItem, "an annotation");
		i18nMgr.setKeyPriority(testSourceBundle, key, 100);
		// key 2
		key = "key.to.stay2";
		i18nItem = i18nMgr.getI18nItem(testSourceBundle, key, testLocale);
		i18nMgr.saveOrUpdateI18nItem(i18nItem, "hello $:key.to.move");
	}

	protected void removeDevToolTests() {
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		// cleanup devtools source/target files
		// 1) source files
		File baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testSourceBundle);
		File testSFile = i18nMgr.getPropertiesFile(testLocale, testSourceBundle, baseDir);
		File sourcePath = testSFile.getParentFile().getParentFile();
		FileUtils.deleteDirsAndFiles(sourcePath, true, true);
		// 2) target files
		baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testTargetBundle);
		File testTFile = i18nMgr.getPropertiesFile(testLocale, testTargetBundle, baseDir);
		File targetPath = testTFile.getParentFile().getParentFile();
		FileUtils.deleteDirsAndFiles(targetPath, true, true);
		// 3) move target files
		baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testMoveTargetBundle);
		File testMFile = i18nMgr.getPropertiesFile(testLocale, testMoveTargetBundle, baseDir);
		File movePath = testMFile.getParentFile().getParentFile().getParentFile();
		FileUtils.deleteDirsAndFiles(movePath, true, true);

	}

	// @Test public void testSortPropertiesTest() {
	// // sort all properties files
	// TranslationDevManager tDMgr = TranslationDevManager.getInstance();
	// tDMgr.sortKeysTask(false);
	// }

	@Test
	public void testGetLostTranslationsFromBranch() {
		String[] referenceLanguages = new String[] { "de", "en" };
		// Path to olat and core dir from the other branch where to lookup for missing translations
		String pathToOlatBranch = "/Volumes/flow/workspace/olat3_60_branch/webapp/WEB-INF/src/";
		String pathToCoreBranch = "/Volumes/flow/workspace/olatcore_60_branch/src/main/java/";
		// Uncomment to run this test. Fix pathes above
		// tDMgr.getLostTranslationsFromBranch(false, referenceLanguages, pathToOlatBranch, pathToCoreBranch );
	}

	@Test
	public void testRemoveDeletedKeysTest() {
		// set languages that is used as reference: all keys there are the keys should not to be deleted
		String[] referenceLanguages = new String[] { "de", "en" };
		// set the languages that should be cleaned up
		Set<String> targetLanguages = I18nModule.getTranslatableLanguageKeys();
		// Set<String> targetLanguages = new HashSet<String>();
		// targetLanguages.add("en");
		tDMgr.removeDeletedKeys(false, referenceLanguages, targetLanguages);
	}

	@Test
	public void testMoveKeyTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			Locale testLocale = i18nMgr.getLocaleOrDefault("de");
			String ktm = "key.to.move";
			i18nMgr.setCachingEnabled(true);
			Properties sourcePropResolved = i18nMgr.getResolvedProperties(testLocale, testSourceBundle);
			Properties sourceProp = i18nMgr.getPropertiesWithoutResolvingRecursively(testLocale, testSourceBundle);
			String matchString = sourceProp.getProperty(ktm);
			assertTrue(sourcePropResolved.getProperty("key.to.stay2").indexOf(matchString) != -1);

			tDMgr.moveKeyToOtherBundle(testSourceBundle, testTargetBundle, ktm);
			sourceProp = i18nMgr.getPropertiesWithoutResolvingRecursively(testLocale, testSourceBundle);
			Properties targetProp = i18nMgr.getPropertiesWithoutResolvingRecursively(testLocale, testTargetBundle);
			assertNull(sourceProp.getProperty(ktm));
			assertNotNull(targetProp.getProperty(ktm));
			Properties sourceMetaProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testSourceBundle);
			Properties targetMetaProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testTargetBundle);
			assertNull(sourceMetaProp.getProperty(ktm + I18nManager.METADATA_ANNOTATION_POSTFIX));
			assertNull(sourceMetaProp.getProperty(ktm + I18nManager.METADATA_KEY_PRIORITY_POSTFIX));
			assertTrue(targetMetaProp.containsKey(ktm + I18nManager.METADATA_ANNOTATION_POSTFIX));
			assertEquals("100", targetMetaProp.getProperty(ktm + I18nManager.METADATA_KEY_PRIORITY_POSTFIX));
			// check for changed references in value
			// if correctly done, should still be resolvable
			assertTrue(sourcePropResolved.getProperty("key.to.stay2").indexOf(matchString) != -1);
			tDMgr.logToFile("moveKey");
		}
	}

	@Test
	public void testMovePackageTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			tDMgr.movePackageTask(testSourceBundle, testTargetBundle);
			i18nMgr.clearCaches();
			Properties sourceProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testSourceBundle);
			assertTrue(sourceProp.isEmpty());
			Properties targetProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testTargetBundle);
			assertFalse(targetProp.isEmpty());
			tDMgr.logToFile("movePackage");
		}
	}

	@Test
	public void testMovePackageByMovingSingleKeysTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			tDMgr.movePackageByMovingSingleKeysTask(testSourceBundle, testTargetBundle);
			i18nMgr.clearCaches();
			Properties sourceProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testSourceBundle);
			assertTrue(sourceProp.isEmpty());
			Properties targetProp = i18nMgr.getPropertiesWithoutResolvingRecursively(null, testTargetBundle);
			assertFalse(targetProp.isEmpty());
			tDMgr.logToFile("movePackageSingle");
		}
	}

	@Test
	public void testMergePackageTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			tDMgr.mergePackageTask(testSourceBundle, testTargetBundle);
			i18nMgr.clearCaches();
			assertTrue(i18nMgr.getPropertiesWithoutResolvingRecursively(null, testSourceBundle).isEmpty());
			// TODO: manipulate source/target first, merge and check if all is in target!
			// try to merge existing key
		}
	}

	@Test
	public void testRenameLanguageTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			// create source
			Locale xxLocale = new Locale("xx");
			String key = "key.to.move";
			I18nItem i18nItem = i18nMgr.getI18nItem(testSourceBundle, key, xxLocale);
			i18nMgr.saveOrUpdateI18nItem(i18nItem, "I have to go");
			i18nMgr.setAnnotation(i18nItem, "an annotation");
			i18nMgr.setKeyPriority(testSourceBundle, key, 100);
			// copy to target
			Locale yyLocale = new Locale("yy");
			tDMgr.renameLanguageTask(xxLocale, yyLocale);
			i18nMgr.clearCaches();
			// test
			Properties deletedProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(xxLocale, testSourceBundle);
			assertTrue(deletedProperties.isEmpty());
			Properties targetProp = i18nMgr.getPropertiesWithoutResolvingRecursively(yyLocale, testSourceBundle);
			assertFalse(targetProp.isEmpty());
		}
	}

	// TODO: RH: remove this or really test for correct finding?!
	@Test
	public void testGetDouplicateKeys() {
		tDMgr.getDouplicateKeys();
	}

	// TODO: RH: remove this or really test for correct finding?!
	@Test
	public void testGetDouplicateValues() {
		tDMgr.getDouplicateValues();
	}

	@Test
	public void testMoveLanguageTask() {
		if (I18nModule.isTransToolEnabled()) {
			prepareDevToolTests();
			String srcPath = I18nModule.getTransToolApplicationLanguagesSrcDir().getAbsolutePath() + "/../../test/java/org/olat/core/util/i18n/junittestdata/devtools";
			String targetPath = srcPath + "/../movetarget";
			// only copy: target should exist
			Locale mvLocale = i18nMgr.getLocaleOrDefault("de");
			// test before move
			Properties sourceProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testSourceBundle);
			assertFalse(sourceProperties.isEmpty());
			Properties moveProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testMoveTargetBundle);
			assertTrue(moveProperties.isEmpty());
			// copy
			tDMgr.moveLanguageTask(mvLocale, srcPath, targetPath, false);
			// test after copy
			sourceProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testSourceBundle);
			assertFalse(sourceProperties.isEmpty());
			moveProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testMoveTargetBundle);
			assertFalse(moveProperties.isEmpty());
			// move
			tDMgr.moveLanguageTask(mvLocale, srcPath, targetPath, true);
			// test after move
			sourceProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testSourceBundle);
			assertTrue(sourceProperties.isEmpty());
			moveProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(mvLocale, testMoveTargetBundle);
			assertFalse(moveProperties.isEmpty());
		}
	}

	// remove after execution!
	@Test
	public void testRemoveXKeyTask() {
		tDMgr.removeXKeysTask(false);
		tDMgr.logToFile("XKeys");
	}

	// remove after execution!
	@Test
	public void testRemoveTodoKeyTask() {
		tDMgr.removeTodoKeysTask(false);
		tDMgr.logToFile("todoKeys");
	}

	// remove after execution!
	@Test
	public void testRemoveEmptyKeysTask() {
		tDMgr.removeEmptyKeysTask(false);
		tDMgr.logToFile("emptyKeys");
	}

	// remove after execution!
	@Test
	public void testRemoveReferenceLanguageCopiesTask() {
		if (I18nModule.isTransToolEnabled()) {
			tDMgr.removeReferenceLanguageCopiesTask(false);
			tDMgr.logToFile("refLangCopied");
		}
	}

	/**
	 * Test method i18nManager.searchForAvailableLanguages()
	 */
	@Test
	public void testSearchForAvailableLanguages() {
		if (I18nModule.isTransToolEnabled()) {
			// Try to load i18n files and a jar from the testdata dir
			File testDataDir = new File(I18nModule.getTransToolApplicationLanguagesSrcDir(), "/../../test/java/org/olat/core/util/i18n/junittestdata/");
			assertTrue(testDataDir.exists());
			Set<String> foundLanguages = i18nMgr.searchForAvailableLanguages(testDataDir);
			// Set must contain some LocalStrings file:
			assertTrue(foundLanguages.contains("de"));
			assertTrue(foundLanguages.contains("en"));
			assertTrue(foundLanguages.contains("pt_BR"));
			// Set must contain some LocaleStrings from the jar package
			assertTrue(foundLanguages.contains("fr"));
			assertTrue(foundLanguages.contains("vi"));
			assertTrue(foundLanguages.contains("zh_CN"));
			// Final check
			assertEquals(6, foundLanguages.size());
		} else {
			Set<String> foundLanguages = I18nModule.getAvailableLanguageKeys();
			// Set must contain some LocaleStrings from the jar package
			assertTrue(foundLanguages.contains("fr"));
			assertTrue(foundLanguages.contains("vi"));
			assertTrue(foundLanguages.contains("zh_CN"));
		}
	}

	/**
	 * Test method i18nManager.searchForBundleNamesContainingI18nFiles()
	 */
	@Test
	public void testSearchForBundleNamesContainingI18nFiles() {
		long start = System.currentTimeMillis();
		List<String> foundBundles = i18nMgr.searchForBundleNamesContainingI18nFiles();
		long end = System.currentTimeMillis();
		log.info("Searching for " + foundBundles.size() + " bundles on OLAT source path took me " + (end - start) + "ms", "testSearchForBundleNamesContainingI18nFiles");
		// Must contain packages from core
		assertTrue(foundBundles.contains("org.olat.core"));
		assertTrue(foundBundles.contains("org.olat.core.gui.control"));
		assertTrue(foundBundles.contains("org.olat.core.commons.fullWebApp"));
		// Must contain packages from olat app
		assertTrue(foundBundles.contains("org.olat"));
		assertTrue(foundBundles.contains("org.olat.course"));
		// Must contain packages from non olat
		assertTrue(foundBundles.contains("com.xyz.demoextension"));
	}

	/**
	 * Test method i18nManager.buildI18nFilename()
	 */
	@Test
	public void testBuildI18nFilename() {
		String overlay = I18nModule.getOverlayName();
		String testFileName = i18nMgr.buildI18nFilename(new Locale("de"));
		assertEquals("LocalStrings_de.properties", testFileName);
		testFileName = i18nMgr.buildI18nFilename(new Locale("de", "", "__" + overlay));
		assertEquals("LocalStrings_de__" + overlay + ".properties", testFileName);
		testFileName = i18nMgr.buildI18nFilename(new Locale("de", "CH"));
		assertEquals("LocalStrings_de_CH.properties", testFileName);
		testFileName = i18nMgr.buildI18nFilename(new Locale("de", "CH", "VENDOR"));
		assertEquals("LocalStrings_de_CH_VENDOR.properties", testFileName);
		testFileName = i18nMgr.buildI18nFilename(new Locale("de", "CH", "VENDOR__" + overlay));
		assertEquals("LocalStrings_de_CH_VENDOR__" + overlay + ".properties", testFileName);
	}

	/**
	 * Test methods i18nManager.getLocaleOrFallback()
	 */
	@Test
	public void testGetLocaleOrFallback() {
		// Normal case
		Locale locale = i18nMgr.getLocaleOrDefault("de");
		assertNotNull(locale);
		assertEquals(i18nMgr.getLocaleOrNull("de"), locale);
		// Unexisting locale case
		locale = i18nMgr.getLocaleOrDefault("xy");
		assertEquals(I18nModule.getDefaultLocale(), locale);
		// Test trying to get overlay via getLocale method which should not return the overlay
		Locale overlay = i18nMgr.getLocaleOrDefault("de__" + I18nModule.getOverlayName());
		assertEquals(I18nModule.getDefaultLocale(), overlay);
		overlay = i18nMgr.getLocaleOrDefault("zh_CN__" + I18nModule.getOverlayName());
		assertEquals(I18nModule.getDefaultLocale(), overlay);
	}

	/**
	 * Test methods i18nManager.createLocal()
	 */
	@Test
	public void testCreateLocale() {
		// standard locale
		Locale loc = i18nMgr.createLocale("de");
		assertNotNull(loc);
		assertEquals("de", loc.getLanguage());
		assertEquals("", loc.getCountry());
		assertEquals("", loc.getVariant());
		// with country
		loc = i18nMgr.createLocale("de_CH");
		assertNotNull(loc);
		assertEquals("de", loc.getLanguage());
		assertEquals("CH", loc.getCountry());
		assertEquals("", loc.getVariant());
		// with variant
		loc = i18nMgr.createLocale("de_CH_ZH");
		assertNotNull(loc);
		assertEquals("de", loc.getLanguage());
		assertEquals("CH", loc.getCountry());
		assertEquals("ZH", loc.getVariant());
		// with variant but no country
		loc = i18nMgr.createLocale("de__VENDOR");
		assertNotNull(loc);
		assertEquals("de", loc.getLanguage());
		assertEquals("", loc.getCountry());
		assertEquals("VENDOR", loc.getVariant());
		//
		// With overlay
		// with language
		String overlay = I18nModule.getOverlayName();
		loc = i18nMgr.createLocale("de");
		Locale over = i18nMgr.createOverlay(loc);
		assertEquals("de____" + overlay, over.toString());
		assertEquals(i18nMgr.createOverlayKeyForLanguage(loc.toString()), i18nMgr.getLocaleKey(over));
		// with country
		loc = i18nMgr.createLocale("de_CH");
		over = i18nMgr.createOverlay(loc);
		assertEquals("de_CH___" + overlay, over.toString());
		assertEquals(i18nMgr.createOverlayKeyForLanguage(loc.toString()), i18nMgr.getLocaleKey(over));
		// with variant
		loc = i18nMgr.createLocale("de_CH_ZH");
		over = i18nMgr.createOverlay(loc);
		assertEquals("de_CH_ZH__" + overlay, over.toString());
		assertEquals(i18nMgr.createOverlayKeyForLanguage(loc.toString()), i18nMgr.getLocaleKey(over));
	}

	/**
	 * Test methods i18nManager.getLanguageTranslated() i18nManager.getLanguageInEnglish()
	 */
	@Test
	public void testGetLanguageIn() {
		assertEquals("German", i18nMgr.getLanguageInEnglish("de", false));
		assertEquals("Deutsch", i18nMgr.getLanguageTranslated("de", false));
		assertEquals("English", i18nMgr.getLanguageInEnglish("en", false));
		assertEquals("English", i18nMgr.getLanguageTranslated("en", false));
	}

	/**
	 * Test methods i18nManager.getLanguageTranslated() i18nManager.getLanguageInEnglish()
	 */
	@Test
	public void testGetPropertyFile() {
		if (I18nModule.isTransToolEnabled()) {
			File baseDir = I18nModule.getPropertyFilesBaseDir(i18nMgr.getLocaleOrDefault("de"), "org.olat.core");
			File file = i18nMgr.getPropertiesFile(i18nMgr.getLocaleOrDefault("de"), "org.olat.core", baseDir);
			assertTrue(file.exists());
			// test get metadata file
			file = i18nMgr.getPropertiesFile(null, "org.olat.core", baseDir);
			assertNotNull(file);
			try {
				file = i18nMgr.getPropertiesFile(i18nMgr.getLocaleOrDefault("de"), null, baseDir);
				fail("exception expected for NULL bundle");
			} catch (AssertException e) {
				// do nothing, expected
			}
			// test with file that does not exist
			file = i18nMgr.getPropertiesFile(i18nMgr.getLocaleOrDefault("de"), "hello.world", baseDir);
			assertFalse(file.exists());
		}
	}

	/**
	 * Test methods i18nManager.getProperties(), i18nManager.saveOrUpdateProperties() and i18nManager.deleteProperties()
	 */
	@Test
	public void testGetSaveOrUpdateAndDeleteProperties() {
		// test with existing files
		Properties props = i18nMgr.getResolvedProperties(i18nMgr.getLocaleOrDefault("de"), "org.olat.core");
		assertNotNull(props);
		assertTrue(props.size() > 0);
		// test with non existing files
		String testNewBundle = "org.olat.core.util.i18n.junittestdata.new";
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		File baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testNewBundle);
		File testFile = i18nMgr.getPropertiesFile(testLocale, testNewBundle, baseDir);
		// clean first existing files from previous broken testcase
		if (testFile.exists()) {
			i18nMgr.deleteProperties(testLocale, testNewBundle);
		}
		props = i18nMgr.getResolvedProperties(testLocale, testNewBundle);
		assertNotNull(props);
		assertEquals(0, props.size());
		// test deleting of non existing
		assertFalse(testFile.exists());
		i18nMgr.deleteProperties(testLocale, testNewBundle);
		assertFalse(testFile.exists());
		// test saving of non existing
		props.setProperty("test.key", "Hello wörld !");
		i18nMgr.saveOrUpdateProperties(props, testLocale, testNewBundle);
		assertTrue(testFile.exists());
		assertEquals("Hello wörld !", i18nMgr.getLocalizedString(testNewBundle, "test.key", null, testLocale, false, false));
		// test saving of existing
		props = new Properties();
		props.setProperty("hello.world", "&%çest françias, ê alors");
		i18nMgr.saveOrUpdateProperties(props, testLocale, testNewBundle);
		assertTrue(testFile.exists());
		assertEquals("&%çest françias, ê alors", i18nMgr.getLocalizedString(testNewBundle, "hello.world", null, testLocale, false, false));
		assertFalse("Hello wörld !".equals(i18nMgr.getLocalizedString(testNewBundle, "test.key", null, testLocale, false, false)));
		// test various special cases
		props.setProperty("chinesetest", "请选择你的大学");
		props.setProperty("specialtest", "that's like \"really\" bad");
		props.setProperty("multiline", "bla\tbla\nsecond line");
		props.setProperty("html", "<h1>Hello World</h1>");
		props.setProperty("empty.property", "");
		i18nMgr.saveOrUpdateProperties(props, testLocale, testNewBundle);
		i18nMgr.clearCaches();
		assertEquals("请选择你的大学", i18nMgr.getLocalizedString(testNewBundle, "chinesetest", null, testLocale, false, false));
		assertEquals("that's like \"really\" bad", i18nMgr.getLocalizedString(testNewBundle, "specialtest", null, testLocale, false, false));
		assertEquals("bla\tbla\nsecond line", i18nMgr.getLocalizedString(testNewBundle, "multiline", null, testLocale, false, false));
		assertEquals("<h1>Hello World</h1>", i18nMgr.getLocalizedString(testNewBundle, "html", null, testLocale, false, false));
		assertEquals("", i18nMgr.getLocalizedString(testNewBundle, "empty.property", null, testLocale, false, false));
		assertEquals(null, i18nMgr.getLocalizedString(testNewBundle, "not.existing.property", null, testLocale, false, false));
		// test deleting of existing
		i18nMgr.deleteProperties(testLocale, testNewBundle);
		assertFalse(testFile.exists());
		assertFalse("Hello wörld !".equals(i18nMgr.getLocalizedString(testNewBundle, "test.key", null, testLocale, false, false)));
		assertFalse("&%çest françias, ê alors".equals(i18nMgr.getLocalizedString(testNewBundle, "hello.world", null, testLocale, false, false)));
		// clean up
		if (testFile.exists()) {
			i18nMgr.deleteProperties(testLocale, testNewBundle);
		}
		if (testFile.getParentFile().exists()) testFile.getParentFile().delete();
	}

	/**
	 * Test methods i18nManager.getBundlePriority(), i18nManager.getKeyPriority()
	 */
	@Test
	public void testGetBundleAndKeyPriority() {
		// existing bundle prios
		String bundleName = "org.olat.core.util.i18n.junittestdata";
		Properties metadataProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(null, bundleName);
		assertNotNull(metadataProperties);
		assertTrue(metadataProperties.size() > 0);
		assertEquals(900, i18nMgr.getBundlePriority(bundleName));
		assertEquals(20, i18nMgr.getKeyPriority(metadataProperties, "no.need.to.translate.this", bundleName));
		// default values
		bundleName = "org.olat.core.util.i18n.junittestdata.subtest";
		metadataProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(null, bundleName);
		assertNotNull(metadataProperties);
		assertTrue(metadataProperties.size() == 0);
		assertEquals(I18nManager.DEFAULT_BUNDLE_PRIORITY, i18nMgr.getBundlePriority("bla.bla")); // default priority
		assertEquals(I18nManager.DEFAULT_KEY_PRIORITY, i18nMgr.getKeyPriority(metadataProperties, "no.need.to.translate.this", bundleName));
	}

	/**
	 * Test methods i18nManager.getAnnotation()
	 */
	@Test
	public void testGetAnnotation() {
		// existing bundle prios
		String bundleName = "org.olat.core.util.i18n.junittestdata";
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		List<I18nItem> items = i18nMgr.findExistingI18nItems(testLocale, bundleName, false);
		for (I18nItem item : items) {
			String annotation = i18nMgr.getAnnotation(item);
			if (item.getKey().equals("no.need.to.translate.this")) {
				assertEquals("this is an annotation", annotation);
			} else {
				assertNull(annotation);
			}
		}
	}

	/**
	 * Test methods i18nManager.countI18nItems() and i18nManager.countBundles()
	 */
	@Test
	public void testCountI18nItemsAndBundles() {
		I18nModule.initBundleNames(); // remove dirty stuff from previous tests
		int bundleCounter = I18nModule.getBundleNamesContainingI18nFiles().size();
		String testNewBundle = "org.olat.core.util.i18n.junittestdata.new";
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		File baseDir = I18nModule.getPropertyFilesBaseDir(testLocale, testNewBundle);
		File testFile = i18nMgr.getPropertiesFile(testLocale, testNewBundle, baseDir);
		// clean first existing files from previous broken testcase
		if (testFile.exists()) {
			i18nMgr.deleteProperties(testLocale, testNewBundle);
		}
		Properties props = i18nMgr.getResolvedProperties(testLocale, testNewBundle);
		assertEquals(0, i18nMgr.countI18nItems(testLocale, testNewBundle, true));
		assertEquals(0, i18nMgr.countI18nItems(testLocale, testNewBundle, false));
		props.setProperty("key.1", "1");
		props.setProperty("key.2", "2");
		i18nMgr.saveOrUpdateProperties(props, testLocale, testNewBundle);
		assertEquals(2, i18nMgr.countI18nItems(testLocale, testNewBundle, false));
		assertEquals(0, i18nMgr.countI18nItems(i18nMgr.getLocaleOrDefault("en"), testNewBundle, false));
		assertEquals(bundleCounter + 1, I18nModule.getBundleNamesContainingI18nFiles().size());
		// test all bundles
		int allCount = i18nMgr.countI18nItems(testLocale, null, true);
		assertEquals(allCount, i18nMgr.countI18nItems(testLocale, null, false));
		props.remove("key.1");
		i18nMgr.saveOrUpdateProperties(props, testLocale, testNewBundle);
		assertEquals(allCount - 1, i18nMgr.countI18nItems(testLocale, null, false));
		i18nMgr.deleteProperties(testLocale, testNewBundle);
		assertEquals(allCount - 2, i18nMgr.countI18nItems(testLocale, null, false));
		assertEquals(bundleCounter, I18nModule.getBundleNamesContainingI18nFiles().size());
		// count bundles tests
		assertEquals(0, i18nMgr.countBundles("org.olat.core.util.i18n.nonexisting", true));
		assertEquals(1, i18nMgr.countBundles("org.olat.core.util.i18n.ui", true));
		// finds 4: regular: i18n.devtools, i18n.ui; from junit tests: i18n.junittestdata, i18n.junittestdata.subtest
		assertEquals(4, i18nMgr.countBundles("org.olat.core.util.i18n", true));
		assertEquals(0, i18nMgr.countBundles("org.olat.core.util.i18n", false));
		assertEquals(1, i18nMgr.countBundles("org.olat.core.util.i18n.ui", false));
		assertTrue(0 < i18nMgr.countBundles(null, false));
		// clean up
		if (testFile.exists()) {
			i18nMgr.deleteProperties(testLocale, testNewBundle);
		}
		if (testFile.getParentFile().exists()) testFile.getParentFile().delete();
		// tests with existing bundles
		String testBundle = "org.olat.core.util.i18n.junittestdata";
		assertEquals(3, i18nMgr.countI18nItems(testLocale, testBundle, false));
		assertEquals(10, i18nMgr.countI18nItems(testLocale, testBundle, true));
	}

	/**
	 * Test methods i18nManager.findInKeys() i18nManager.findInValues()
	 */
	@Test
	public void testFindInKeysAndFindInValues() {
		Locale testLocale = i18nMgr.getLocaleOrDefault("de");
		// in keys speedtest
		long start = System.currentTimeMillis();
		List<I18nItem> foundTransItems = i18nMgr.findI18nItemsByKeySearch("menu", testLocale, testLocale, null, true);
		long end = System.currentTimeMillis();
		log.info("Searching for term 'menu' took me " + (end - start) + "ms, found " + foundTransItems.size() + " item", "testFindInKeys");
		// in values speedtest
		start = System.currentTimeMillis();
		foundTransItems = i18nMgr.findI18nItemsByValueSearch("OLAT", testLocale, testLocale, null, true);
		int uppercaseSize = foundTransItems.size();
		end = System.currentTimeMillis();
		log.info("Searching for term 'OLAT' took me " + (end - start) + "ms, found " + foundTransItems.size() + " item", "testFindInKeys");
		// uppercase and lowercase must find the same values
		foundTransItems = i18nMgr.findI18nItemsByValueSearch("olat", testLocale, testLocale, null, true);
		assertEquals(uppercaseSize, foundTransItems.size());
		// find in values and regexp tests: * is replaced with regexp,
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("Eingabefehler aufgetreten", testLocale, testLocale, null, true).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("Eingabefehler*aufgetreten", testLocale, testLocale, null, true).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("Eingabefe*ler*aufgetreten", testLocale, testLocale, null, true).size());
		assertEquals(2, i18nMgr.findI18nItemsByValueSearch("Bitte f*llen Sie dieses Feld aus", testLocale, testLocale, null, true).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("Bitte f*llen Sie dieses Feld aus", testLocale, testLocale, "org.olat.core", false).size());
		// escape reserved regexp keywords
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("LMS", testLocale, testLocale, "org.olat.core", false).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("MS", testLocale, testLocale, "org.olat.core", false).size());
		assertEquals(3, i18nMgr.findI18nItemsByValueSearch("*Learning", testLocale, testLocale, "org.olat.core", false).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("Eingaben</i> </br >*<b>bold</b>*<br>_<i>italic</i>_<br>* Listen", testLocale, testLocale, null, true).size());
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("dargestellt werden. Bitte rufen Sie", testLocale, testLocale, null, true).size());
		// multi line value search
		assertEquals(1, i18nMgr.findI18nItemsByValueSearch("die Lernplattform OLAT versandt.", testLocale, testLocale, null, true).size());
	}

	/**
	 * Test methods i18nManager.findMissingI18nItems()
	 */
	@Test
	public void testFindMissingI18nItems() {
		Locale sourceLocale = i18nMgr.getLocaleOrDefault("de");
		Locale targetLocale = i18nMgr.getLocaleOrDefault("de");
		String notExistsTestBundle = "org.olat.core.util.i18n.junittestdata.notexists";
		assertEquals(0, i18nMgr.findMissingI18nItems(sourceLocale, targetLocale, null, true).size());
		assertEquals(0, i18nMgr.findMissingI18nItems(sourceLocale, targetLocale, notExistsTestBundle, true).size());
		assertEquals(0, i18nMgr.findMissingI18nItems(sourceLocale, targetLocale, notExistsTestBundle, false).size());
		targetLocale = new Locale("xy");
		int total = i18nMgr.findExistingI18nItems(sourceLocale, null, true).size();
		assertEquals(total, i18nMgr.findMissingI18nItems(sourceLocale, targetLocale, null, true).size());
		assertEquals(total, i18nMgr.findExistingAndMissingI18nItems(sourceLocale, targetLocale, null, true).size());
	}

	/**
	 * Test methods i18nManager inline translation tool and InlineTranslationInterceptHandlerController
	 */
	@Test
	public void testInlineTranslationReplaceLocalizationMarkupWithHTML() {
		// enable inline translation markup
		i18nMgr.setMarkLocalizedStringsEnabled(null, true);
		Translator inlineTrans = Util.createPackageTranslator(InlineTranslationInterceptHandlerController.class, i18nMgr.getLocaleOrNull("de"));
		URLBuilder inlineTranslationURLBuilder = new jUnitURLBuilder();
		String testBundle = "org.olat.core.util.i18n.junittestdata";
		String testKey = "no.need.to.translate.this";
		String rawtext1 = "Lorem impsum<b>nice stuff</b>";
		String rawtext2 = "Holderadio <ul>lsdfsdf<y  asdf blblb";
		String combinedKey = testBundle + ":" + testKey;
		// test method that adds identifyers around the translation items
		String plainVanillaWrapped = i18nMgr.getLocalizedString(testBundle, testKey, null, i18nMgr.getLocaleOrNull("de"), false, false);
		String plainVanillaPlain = "just a test";
		Pattern plainVanillaWrappedPattern = Pattern.compile(I18nManager.IDENT_PREFIX + combinedKey + ":([0-9]*?)" + I18nManager.IDENT_START_POSTFIX + plainVanillaPlain
				+ I18nManager.IDENT_PREFIX + combinedKey + ":\\1" + I18nManager.IDENT_END_POSTFIX);
		Matcher m = plainVanillaWrappedPattern.matcher(plainVanillaWrapped);
		assertTrue(m.find());

		// test find-replace translator identifyers with HTML markup
		StringOutput inlineTransLink = new StringOutput();
		String[] args = (combinedKey + ":1000000001").split(":");
		InlineTranslationInterceptHandlerController.buildInlineTranslationLink(args, inlineTransLink, inlineTrans, inlineTranslationURLBuilder);
		// Plain vanilla text
		String convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(plainVanillaWrapped, inlineTranslationURLBuilder,
				inlineTrans);
		assertEquals("<span class=\"b_translation_i18nitem\">" + inlineTransLink.toString() + plainVanillaPlain + "</span>", convertedToHTMLMarkup);
		// Simple link
		String linkOPEN = "<a href=\"http://www.olat.org/bla/blu:bli#bla\" title='funny title' class=\"b_css b_anothercss\">";
		String linkCLOSE = "</a>";
		String inlineSpanOPEN = "<span class=\"b_translation_i18nitem\">";
		String inlineSpanCLOSE = "</span>";
		String translatedWithinLink = linkOPEN + plainVanillaWrapped + linkCLOSE + rawtext1;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinLink, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinLinkExpected = inlineSpanOPEN + inlineTransLink.toString() + linkOPEN + plainVanillaPlain + linkCLOSE + inlineSpanCLOSE + rawtext1;
		assertEquals(convertedWithinLinkExpected, convertedToHTMLMarkup);
		// Simple link with span
		String linkSpanOPEN = "<span class=\"bluber\">";
		String linkSpanCLOSE = "</span>";
		String translatedWithinLinkAndSpan = rawtext2 + linkOPEN + linkSpanOPEN + plainVanillaWrapped + linkSpanCLOSE + linkCLOSE;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinLinkAndSpan, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinLinkAndSpanExpected = rawtext2 + inlineSpanOPEN + inlineTransLink.toString() + linkOPEN + linkSpanOPEN + plainVanillaPlain + linkSpanCLOSE
				+ linkCLOSE + inlineSpanCLOSE;
		assertEquals(convertedWithinLinkAndSpanExpected, convertedToHTMLMarkup);
		// Muliple links
		String translatedWithinMultipleLinks = translatedWithinLink + translatedWithinLinkAndSpan + translatedWithinLinkAndSpan;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinMultipleLinks, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinMultipleLinksExpected = convertedWithinLinkExpected + convertedWithinLinkAndSpanExpected + convertedWithinLinkAndSpanExpected;
		assertEquals(convertedWithinMultipleLinksExpected, convertedToHTMLMarkup);
		// Input elements
		String inputOPEN = "<input type='submit' class=\"bluber\" value=\"";
		String inputCLOSE = "\" />";
		String translatedWithinInput = inputOPEN + plainVanillaWrapped + inputCLOSE + rawtext1;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinInput, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinInputExpected = inlineSpanOPEN + inlineTransLink.toString() + inputOPEN + plainVanillaPlain + inputCLOSE + inlineSpanCLOSE + rawtext1;
		assertEquals(convertedWithinInputExpected, convertedToHTMLMarkup);
		// checkbox elements
		String checkboxOPEN = "<input type='submit' class=\"bluber\" type=\"checkbox\" value=\"";
		String checkboxCLOSE = "\" />";
		String translatedWithinCheckbox = checkboxOPEN + plainVanillaWrapped + checkboxCLOSE + rawtext1;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinCheckbox, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinCheckboxExpected = checkboxOPEN + plainVanillaPlain + checkboxCLOSE + rawtext1;
		assertEquals(convertedWithinCheckboxExpected, convertedToHTMLMarkup);
		// Input and links mixed
		String translatedWithinMultipleLinksAndInput = translatedWithinLink + rawtext1 + translatedWithinInput + translatedWithinLinkAndSpan + rawtext2
				+ translatedWithinInput + translatedWithinLinkAndSpan;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinMultipleLinksAndInput,
				inlineTranslationURLBuilder, inlineTrans);
		String convertedWithinMultipleLinksAndInputExpected = convertedWithinLinkExpected + rawtext1 + convertedWithinInputExpected + convertedWithinLinkAndSpanExpected
				+ rawtext2 + convertedWithinInputExpected + convertedWithinLinkAndSpanExpected;
		assertEquals(convertedWithinMultipleLinksAndInputExpected, convertedToHTMLMarkup);
		// Within element attribute
		String attributeOPEN = "<a href='sdfsdf' title=\"";
		String attributeCLOSE = "\" class=\"b_bluber\">hello world</a>";
		String translatedWithinAttribute = attributeOPEN + plainVanillaWrapped + attributeCLOSE + rawtext1;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedWithinAttribute, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedWithinAttributeExpected = attributeOPEN + plainVanillaPlain + attributeCLOSE + rawtext1;
		assertEquals(convertedWithinAttributeExpected, convertedToHTMLMarkup);
		// Ultimate test
		String translatedUltimate = translatedWithinMultipleLinksAndInput + rawtext1 + translatedWithinAttribute + translatedWithinMultipleLinksAndInput + rawtext2
				+ translatedWithinInput + translatedWithinLinkAndSpan;
		convertedToHTMLMarkup = InlineTranslationInterceptHandlerController.replaceLocalizationMarkupWithHTML(translatedUltimate, inlineTranslationURLBuilder,
				inlineTrans);
		String convertedUltimateExpected = convertedWithinMultipleLinksAndInputExpected + rawtext1 + convertedWithinAttributeExpected
				+ convertedWithinMultipleLinksAndInputExpected + rawtext2 + convertedWithinInputExpected + convertedWithinLinkAndSpanExpected;
		assertEquals(convertedUltimateExpected, convertedToHTMLMarkup);

		// don't do inline translation markup
		i18nMgr.setMarkLocalizedStringsEnabled(null, false);
	}

	/**
	 * Test method i18nManager.getLocalizedString() with resolvePropertiesInternalKeys()
	 */
	@Test
	public void testResolvePropertiesInternalKeys() {
		String bundleName = "org.olat.core.util.i18n.junittestdata.subtest";
		Locale locale = Locale.GERMAN;
		// first tests with cache enabled
		i18nMgr.setCachingEnabled(true);
		assertEquals("Hello world, this is just a test", i18nMgr.getLocalizedString(bundleName, "recursive.test", null, locale, false, true));
		assertEquals("Hello world, this is just a test (dont translate it)", i18nMgr.getLocalizedString(bundleName, "recursive.test2", null, locale, false, true));
		// test with . and ,
		assertEquals("Hello world, this is just a test. Really dont translate it, because this is just a test.",
				i18nMgr.getLocalizedString(bundleName, "recursive.test3", null, locale, false, true));
		// a combined word
		assertEquals("Hello world, this is obersuperduper", i18nMgr.getLocalizedString(bundleName, "recursive.test4", null, locale, false, true));
		// a endless loop that can't be resolved
		assertEquals(
				"Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop $org.olat.core.util.i18n.junittestdata.subtest:recursive.test5",
				i18nMgr.getLocalizedString(bundleName, "recursive.test5", null, locale, false, true));

		// same tests without cache
		i18nMgr.setCachingEnabled(false);
		assertEquals("Hello world, this is just a test", i18nMgr.getLocalizedString(bundleName, "recursive.test", null, locale, false, true));
		assertEquals("Hello world, this is just a test (dont translate it)", i18nMgr.getLocalizedString(bundleName, "recursive.test2", null, locale, false, true));
		// test with . and ,
		assertEquals("Hello world, this is just a test. Really dont translate it, because this is just a test.",
				i18nMgr.getLocalizedString(bundleName, "recursive.test3", null, locale, false, true));
		// a combined word
		assertEquals("Hello world, this is obersuperduper", i18nMgr.getLocalizedString(bundleName, "recursive.test4", null, locale, false, true));
		// a endless loop that can't be resolved
		assertEquals("Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop Loop $org.olat.core.util.i18n.junittestdata.subtest:recursive.test5",
				i18nMgr.getLocalizedString(bundleName, "recursive.test5", null, locale, false, true));
		// note: it's ok that the results differ here. Important is that the loop
		// eventually terminates. when caching is enabled, the resulst are
		// accumulated from the previous iteration, this is different in non-caching
		// mode. But this should not have a negative effect on real-world scenarios

		i18nMgr.setCachingEnabled(true);
	}

	/**
	 * Description:<br>
	 * Dummy URL builder
	 * <P>
	 * Initial Date: 22.09.2008 <br>
	 * 
	 * @author gnaegi
	 */
	private class jUnitURLBuilder extends URLBuilder {
		public jUnitURLBuilder() {
			super(null, null, null, null);
		}

		public void buildURI(StringOutput buf, String[] keys, String[] values) {
			buf.append("http://do.test.com");
		}

	}

}

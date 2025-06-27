package dev.franke.felipe.api30_automation_api.automation.selenium.impl;

import dev.franke.felipe.api30_automation_api.automation.selenium.contract.AutomationRunner;
import dev.franke.felipe.api30_automation_api.automation.selenium.exception.ExecutionException;
import dev.franke.felipe.api30_automation_api.automation.selenium.exception.InvalidWebDriverTitleException;
import dev.franke.felipe.api30_automation_api.automation.credentials.domain.BraspagCredentials;
import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.CieloMerchant;
import dev.franke.felipe.api30_automation_api.automation.merchant_data.domain.EstablishmentCodeImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class AutomationRunnerImpl implements AutomationRunner {

    private static final Logger LOG = Logger.getLogger(AutomationRunnerImpl.class.getName());

    private static final String BRASPAG_ENTRYPOINT = "https://admin.braspag.com.br/Admin/Home";
    private static final String BRASPAG_SEARCH_ESTABLISHMENTS_PAGE = "https://admin.braspag.com.br/EcommerceCielo/List";
    private static final String BRASPAG_WEBHOOK_BASE_URL = "https://admin.braspag.com.br/Transactional/Notifications/";
    private static final String BRASPAG_IP_BASE_URL = "https://admin.braspag.com.br/IpManager/EditReliableIp?merchantId=";
    private static final byte MAX_WAITING_MINUTES_BEFORE_TIMEOUT = 3;
    private static final int WAITING_TIME_AFTER_CLICK_IN_MS = 3000;

    private WebDriver webDriver;
    private final BraspagCredentials credentials;
    private final EstablishmentCodeImpl establishmentCode;
    private final Wait<WebDriver> waitStrategy;
    private CieloMerchant cieloMerchant;

    public AutomationRunnerImpl(String username, String password, String establishmentNumber) {
        setUpDriver();
        this.waitStrategy = new WebDriverWait(webDriver, Duration.ofMinutes(MAX_WAITING_MINUTES_BEFORE_TIMEOUT));
        this.credentials = new BraspagCredentials(username, password);
        this.establishmentCode = new EstablishmentCodeImpl(establishmentNumber);
    }

    @Override
    public CieloMerchant run() {
        LOG.info("[MAIN-RUN] Running automation");
        try {
            performLogin();
            searchForEc();
            getData();
            return cieloMerchant;
        } catch (Exception exception) {
            LOG.warning("[MAIN-RUN] Got an error: " + exception.getMessage());
            LOG.warning("[MAIN-RUN] Throwing ExecutionException");
            throw new ExecutionException(exception.getMessage());
        } finally {
            LOG.info("[MAIN-RUN] Quitting the WebDriver");
            quitDriver();
            LOG.info("[MAIN-RUN] Finishing execution");
        }
    }

    private void quitDriver() {
        try {
            LOG.info("[QUIT-DRIVER] Attempting to quit driver");
            webDriver.quit();
            LOG.info("[QUIT-DRIVER] Driver quit");
        } catch (Exception exception) {
            LOG.warning("[QUIT-DRIVER] Error while quitting driver! Message: " + exception.getMessage());
        }
    }

    private void setUpDriver() {
        LOG.info("[INITIALIZATION] Starting to setup ChromeOptions..");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--no-sandbox");
        webDriver = new ChromeDriver(chromeOptions);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofMinutes(MAX_WAITING_MINUTES_BEFORE_TIMEOUT));
        LOG.info("[INITIALIZATION] ChromeOptions and WebDriver defined!");
    }

    private void checkTitle() {
        LOG.info("[CHECK-TITLE] Checking if title is valid..");
        String title = webDriver.getTitle();
        checkTitleIsNullOrBlank(title);
        checkTitleIsInternalError(title);
        LOG.info("[CHECK-TITLE] Checked. Title is valid!");
    }

    private void performLogin() throws InterruptedException {
        LOG.info("[PERFORM-LOGIN] Starting to perform Login operation");
        goToBraspagLoginPage();
        sendUsernameKeys();
        sendPasswordKeys();
        clickEnter();
        LOG.info("[PERFORM-LOGIN] Login operation performed!");
    }

    private void searchForEc() throws InterruptedException {
        LOG.info("[EC-SEARCH] Starting to perform EC search");
        LOG.info("[EC-SEARCH] NUMBER: " + establishmentCode.establishmentNumber());
        goToSearchMerchantsPage();
        fillMerchantEstablishmentCode();
        clearStartDate();
        clickSearchButton();
        checkEstablishment();
        clickEstablishmentLink();
        LOG.info("[EC-SEARCH] EC search performed!");
    }

    private void getData() {
        LOG.info("[GET-DATA] Starting to go into the Merchant details page");
        defineMerchantData();
        LOG.info("[GET-DATA] Merchant Data is defined and available!");
    }

    private void goToBraspagLoginPage() {
        webDriver.get(BRASPAG_ENTRYPOINT);
    }

    private void sendUsernameKeys() {
        WebElement username = webDriver.findElement(By.id("param1"));
        waitStrategy.until(driver -> username.isDisplayed());
        username.sendKeys(credentials.username());
    }

    private void sendPasswordKeys() {
        WebElement password = webDriver.findElement(By.id("param2"));
        waitStrategy.until(driver -> password.isDisplayed());
        password.sendKeys(credentials.password());
    }

    private void clickEnter() throws InterruptedException {
        WebElement enter = webDriver.findElement(By.id("enter"));
        waitStrategy.until(driver -> enter.isDisplayed());
        enter.click();
        Thread.sleep(WAITING_TIME_AFTER_CLICK_IN_MS);
        checkTitle();
    }

    private void goToSearchMerchantsPage() {
        webDriver.get(BRASPAG_SEARCH_ESTABLISHMENTS_PAGE);
    }

    private void fillMerchantEstablishmentCode() {
        WebElement establishmentNumberInput = webDriver.findElement(By.id("EcNumber"));
        waitStrategy.until(driver -> establishmentNumberInput.isDisplayed());
        establishmentNumberInput.sendKeys(establishmentCode.establishmentNumber());
    }

    private void clearStartDate() {
        WebElement startDate = webDriver.findElement(By.id("StartDate"));
        waitStrategy.until(driver -> startDate.isDisplayed());
        startDate.clear();
    }

    private void clickSearchButton() throws InterruptedException {
        WebElement search = webDriver.findElement(By.id("buttonSearch"));
        waitStrategy.until(driver -> search.isDisplayed());
        search.click();
        Thread.sleep(WAITING_TIME_AFTER_CLICK_IN_MS);
        checkTitle();
    }

    private void checkEstablishment() throws InterruptedException {
        WebElement resultElement = getResultElement();
        checkResultElement(resultElement);
    }

    private void checkResultElement(WebElement resultElement) {
        String innerText = elementInnerText(resultElement);
        checkResultInnerTextIsValid(innerText);
        checkEstablishmentIsFound(innerText);
    }

    private void checkEstablishmentIsFound(String resultInnerText) {
        if (!establishmentFound(resultInnerText)) {
            throw new ExecutionException("Establishment Not Found");
        }
    }

    private boolean establishmentFound(String resultInnerText) {
        final String textMatchingStringOne = "1";
        return resultInnerText.contains(textMatchingStringOne);
    }

    private void checkResultInnerTextIsValid(String resultInnerText) {
        if (!resultInnerTextIsValid(resultInnerText)) {
            throw new ExecutionException("The result inner text is not valid");
        }
    }

    private boolean resultInnerTextIsValid(String resultInnerText) {
        return resultInnerText != null && !resultInnerText.isBlank();
    }

    private WebElement getResultElement() throws InterruptedException {
        var titles = waitStrategy.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("adm-title")));
        titles = waitStrategy.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("adm-title"), 1));
        Thread.sleep(WAITING_TIME_AFTER_CLICK_IN_MS);
        final byte elementWithResultTextIndex = 1;
        try {
            return titles.get(elementWithResultTextIndex);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not load Establishment Code");
        }
    }

    private void clickEstablishmentLink() throws InterruptedException {
        var links = waitStrategy.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("a")));
        var correctElement = findEstablishmentCodeLink(links);
        correctElement.click();
        Thread.sleep(WAITING_TIME_AFTER_CLICK_IN_MS);
        checkTitle();
    }

    private WebElement findEstablishmentCodeLink(List<WebElement> links) {
        for (WebElement link : links) {
            String titleAttribute = link.getAttribute("title");
            if (correctTitle(titleAttribute)) {
                return link;
            }
        }
        throw new ExecutionException("Could not find the correct establishment link to click");
    }

    private boolean correctTitle(String title) {
        return title != null && title.contains("Ver Detalhes");
    }

    private void checkFormContent(WebElement element) {
        if (!formContentIsValid(element)) {
            throw new ExecutionException("Could not get the inner text of element");
        }
    }

    private boolean formContentIsValid(WebElement element) {
        return element != null && element.getAttribute("innerText") != null;
    }

    private void defineMerchantData() {
        List<WebElement> formElements = waitFormControlStaticContent();
        List<WebElement> listElements = waitListElements();
        UUID merchantId = defineMerchantId(formElements);
        String document = defineDocument(formElements);
        String documentType = defineDocumentType(document);
        String merchantName = defineMerchantName(formElements);
        final byte tokenizationIndex = 102;
        final byte velocityIndex = 103;
        final byte recurrencyIndex = 104;
        final byte zeroAuthIndex = 106;
        final byte binQueryIndex = 107;
        final byte selectiveAuthIndex = 108;
        final byte automaticCancellationIndex = 109;
        final byte forceAuthIndex = 110;
        final byte mtlsIndex = 111;
        boolean merchantBlocked = defineMerchantIsBlocked();
        boolean pixEnabled = definePixEnabled();
        boolean antifraudEnabled = defineAntifraudEnabled(listElements);
        boolean tokenizationEnabled = defineServiceEnabled(listElements, tokenizationIndex, "tokenizationEnabled");
        boolean velocityEnabled = defineServiceEnabled(listElements, velocityIndex, "velocityEnabled");
        boolean recurrencyEnabled = defineServiceEnabled(listElements, recurrencyIndex, "recurrencyEnabled");
        boolean zeroAuthEnabled = defineServiceEnabled(listElements, zeroAuthIndex, "zeroAuthEnabled");
        boolean binQueryEnabled = defineServiceEnabled(listElements, binQueryIndex, "binQueryEnabled");
        boolean selectiveAuthEnabled = defineServiceEnabled(listElements, selectiveAuthIndex, "selectiveAuthEnabled");
        boolean automaticCancellationEnabled = defineServiceEnabled(listElements, automaticCancellationIndex, "automaticCancellationEnabled");
        boolean forceAuthEnabled = defineServiceEnabled(listElements, forceAuthIndex, "forceAuthEnabled");
        boolean mtlsEnabled = defineServiceEnabled(listElements, mtlsIndex, "mtlsEnabled");
        boolean webhookEnabled = defineWebhookEnabled(merchantId);
        byte ips = (byte) defineIpCount(merchantId);
        cieloMerchant = new CieloMerchant(
                establishmentCode.establishmentNumber(),
                merchantId,
                documentType,
                document,
                merchantName,
                merchantBlocked,
                pixEnabled,
                antifraudEnabled,
                tokenizationEnabled,
                velocityEnabled,
                recurrencyEnabled,
                zeroAuthEnabled,
                binQueryEnabled,
                selectiveAuthEnabled,
                automaticCancellationEnabled,
                forceAuthEnabled,
                mtlsEnabled,
                webhookEnabled,
                ips
        );
    }

    private boolean definePixEnabled() {
        WebElement pix = webDriver.findElement(By.id("buttonEditCieloPix"));
        waitStrategy.until(driver -> pix.isDisplayed());
        String pixText = pix.getAttribute("innerText");
        checkAttributeIsNotNullOrBlank(pixText);
        assert pixText != null;
        return pixEnabled(pixText);
    }

    private boolean pixEnabled(String pixText) {
        return pixText.contains("Desabilitar");
    }

    private void checkAttributeIsNotNullOrBlank(String attribute) {
        if (!attributeIsNotNullOrBlank(attribute)) {
            throw new ExecutionException("Attribute has invalid parameters");
        }
    }

    private boolean attributeIsNotNullOrBlank(String attribute) {
        return attribute != null && !attribute.isBlank();
    }

    private boolean defineMerchantIsBlocked() {
        WebElement merchantIsBlockedElement = webDriver.findElement(By.className("check-box"));
        waitStrategy.until(driver -> merchantIsBlockedElement.isDisplayed());
        return merchantIsBlockedElement.getAttribute("checked") != null;
    }

    private String defineMerchantName(List<WebElement> formElements) {
        final byte merchantNameIndex = 3;
        try {
            WebElement element = formElements.get(merchantNameIndex);
            checkFormContent(element);
            return element.getAttribute("innerText");
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get merchant name");
        }
    }

    private String defineDocumentType(String document) {
        if (document.length() == 14) {
            return "CPF";
        }
        return "CNPJ";
    }

    private String defineDocument(List<WebElement> formElements) {
        final byte documentIndex = 2;
        try {
            WebElement element = formElements.get(documentIndex);
            checkFormContent(element);
            return element.getAttribute("innerText");
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get document");
        }
    }

    private UUID defineMerchantId(List<WebElement> formElements) {
        final byte merchantIdIndex = 1;
        try {
            WebElement element = formElements.get(merchantIdIndex);
            checkFormContent(element);
            return UUID.fromString(Objects.requireNonNull(element.getAttribute("innerText")));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ExecutionException("Could not convert element to UUID type in Merchant Id");
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get Merchant Id");
        }
    }

    private String elementInnerText(WebElement element) {
        return element.getAttribute("innerText");
    }

    private boolean antifraudTextContainsEnabledText(String text) {
        return text.contains("Habilitado");
    }

    private boolean defineAntifraudEnabled(List<WebElement> listElements) {
        final byte antifraudIndex = 101;
        try {
            WebElement element = listElements.get(antifraudIndex);
            checkFormContent(element);
            return antifraudTextContainsEnabledText(elementInnerText(element));
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get Antifraud enabled");
        }
    }

    private boolean textIndicatesEnabledProduct(String text) {
        return !text.contains("não definido") && !text.contains("Desabilitado");
    }

    private boolean defineServiceEnabled(List<WebElement> listElements, byte index, String productName) {
        try {
            WebElement element = listElements.get(index);
            checkFormContent(element);
            return textIndicatesEnabledProduct(elementInnerText(element));
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get '" + productName + "'");
        }
    }

    private List<WebElement> waitFormControlStaticContent() {
        return waitStrategy.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("form-control-static")));
    }

    private List<WebElement> waitListElements() {
        return waitStrategy.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("li")));
    }

    private int defineIpCount(UUID merchantId) {
        goToIpPage(merchantId);
        List<WebElement> admTitleElements = getAllAdmTitleElements();
        WebElement ipResultElement = getIpResultElement(admTitleElements);
        checkIpResultInnerTextIsValid(ipResultElement);
        return getIpResultInteger(ipResultElement);
    }

    private int getIpResultInteger(WebElement element) {
        try {
            return Integer.parseInt(elementInnerText(element).split("Resultado da Busca: ")[1].split(" ")[0]);
        } catch (NumberFormatException numberFormatException) {
            throw new ExecutionException("IP Count is not parsable");
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("IP Result Text is not parsable");
        }
    }

    private void checkIpResultInnerTextIsValid(WebElement ipResultElement) {
        if (!innerTextIsNotNullOrBlank(elementInnerText(ipResultElement))) {
            throw new ExecutionException("IP result text is blank or null");
        }
    }

    private WebElement getIpResultElement(List<WebElement> admTitleElements) {
        try {
            return admTitleElements.get(1);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new ExecutionException("Could not get IP count");
        }
    }

    private List<WebElement> getAllAdmTitleElements() {
        return waitStrategy.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("adm-title")));
    }

    private boolean defineWebhookEnabled(UUID merchantId) {
        goToNotificationUrlPage(merchantId);
        WebElement notificationUrl = notificationUrlElement();
        return innerTextIsNotNullOrBlank(elementInnerText(notificationUrl));
    }

    private boolean innerTextIsNotNullOrBlank(String innerText) {
        return innerText != null && !innerText.isBlank();
    }

    private WebElement notificationUrlElement() {
        WebElement notificationUrl = webDriver.findElement(By.id("notificationUrl"));
        waitStrategy.until(driver -> notificationUrl.isDisplayed());
        return notificationUrl;
    }

    private void goToIpPage(UUID merchantId) {
        String baseUrl = BRASPAG_IP_BASE_URL + merchantId.toString();
        webDriver.get(baseUrl);
        checkTitle();
    }

    private void goToNotificationUrlPage(UUID merchantId) {
        String baseUrl = BRASPAG_WEBHOOK_BASE_URL + merchantId.toString();
        webDriver.get(baseUrl);
        checkTitle();
    }

    private void checkTitleIsInternalError(String title) {
        if (titleIsInternalError(title)) {
            throw new InvalidWebDriverTitleException("Title indicates internal server error");
        }
    }

    private boolean titleIsInternalError(String title) {
        return title.contains("Internal server error");
    }

    private void checkTitleIsNullOrBlank(String title) {
        if (titleIsNullOrBlank(title)) {
            throw new InvalidWebDriverTitleException("Title of the page is null or blank");
        }
    }

    private boolean titleIsNullOrBlank(String title) {
        return title == null || title.isBlank();
    }
}

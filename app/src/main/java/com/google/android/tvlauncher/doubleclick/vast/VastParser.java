package com.google.android.tvlauncher.doubleclick.vast;

import android.support.p001v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.tvlauncher.doubleclick.Clock;
import com.google.android.tvlauncher.doubleclick.proto.nano.AdConfig;
import com.google.android.tvlauncher.doubleclick.proto.nano.VideoCreative;
import com.google.android.tvlauncher.doubleclick.vast.DomDigester;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class VastParser {

    /* renamed from: AD */
    private static final String f155AD = "VAST/Ad/%s";
    static final long AD_VALIDITY_TTL_MS = 3600000;
    private static final String BASE = "VAST";
    private static final String BASE_AD = "VAST/Ad";
    private static final String COMPANION = "VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion";
    private static final String COMPANION_ADS = "VAST/Ad/%s/Creatives/Creative/CompanionAds";
    private static final String[] COMPANION_NONLINEAR = {COMPANION, NON_LINEAR};
    private static final String COMPANION_TRACKING_EVENT = "VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/TrackingEvents/Tracking";
    private static final String COMPANION_TRACKING_EVENTS = "VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/TrackingEvents";
    private static final String CREATIVE = "VAST/Ad/%s/Creatives/Creative";
    private static final String DESTINATION_URL = "VAST/Ad/%s/Creatives/Creative/Linear/VideoClicks/ClickThrough";
    private static final String DURATION = "VAST/Ad/%s/Creatives/Creative/Linear/Duration";
    private static final String EXTENSION = "VAST/Ad/%s/Extensions/Extension";
    private static final String EXTENSIONS_TRACKING_CUSTOM = "VAST/Ad/%s/Extensions/Extension/CustomTracking/Tracking";
    private static final String IMPRESSION = "VAST/Ad/%s/Impression";
    private static final String LINEAR = "Linear";
    private static final String LINEAR_PARAMS = "VAST/Ad/%s/Creatives/Creative/Linear/AdParameters";
    private static final String MEDIA_FILE = "VAST/Ad/%s/Creatives/Creative/Linear/MediaFiles/MediaFile";
    private static final String MEDIA_FILES = "VAST/Ad/%s/Creatives/Creative/Linear/MediaFiles";
    private static final String NONLINEAR_ELEMENT = "NonLinearAds";
    private static final String NON_LINEAR = "VAST/Ad/%s/Creatives/Creative/NonLinearAds/NonLinear";
    private static final String NON_LINEAR_ADS = "VAST/Ad/%s/Creatives/Creative/NonLinearAds";
    private static final String NON_LINEAR_AD_TRACKING_EVENT = "VAST/Ad/%s/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
    private static final String NON_LINEAR_AD_TRACKING_EVENTS = "VAST/Ad/%s/Creatives/Creative/NonLinearAds/TrackingEvents";
    private static final String NON_LINEAR_CLICK_THROUGH = "VAST/Ad/%s/Creatives/Creative/NonLinearAds/NonLinear/NonLinearClickThrough";
    private static final String NON_LINEAR_PARAMS = "VAST/Ad/%s/Creatives/Creative/NonLinearAds/NonLinear/AdParameters";
    private static final String REDIRECT_URL = "VAST/Ad/%s/VASTAdTagURI";
    private static final String SURVEY = "VAST/Ad/%s/Survey";
    private static final String TAG = "VastParser";
    private static final String TRACKING_CLICK = "VAST/Ad/%s/Creatives/Creative/Linear/VideoClicks/ClickTracking";
    private static final String TRACKING_CUSTOM = "VAST/Ad/%s/Creatives/Creative/Linear/VideoClicks/CustomClick";
    private static final String TRACKING_EVENT = "VAST/Ad/%s/Creatives/Creative/Linear/TrackingEvents/Tracking";
    private static final String TRACKING_EVENTS = "VAST/Ad/%s/Creatives/Creative/Linear/TrackingEvents";
    private static final String VAST2_XSD = "/schemas/vast_2.0.1.xsd";
    private static final String VAST3_XSD = "/schemas/vast_3.0.0.xsd";
    private static final String VIDEO_CLICKS = "VAST/Ad/%s/Creatives/Creative/Linear/VideoClicks";
    private final Clock clock;
    private ParserType parserType;
    /* access modifiers changed from: private */
    public int vastVersion;
    private List<VideoCreative.VastXml> videos;

    public enum ParserType {
        INLINE("InLine"),
        WRAPPER("Wrapper");
        
        private final String type;

        ParserType(String type2) {
            this.type = type2;
        }

        public String toString() {
            return this.type;
        }
    }

    public VastParser() {
        this(new SystemClock());
    }

    VastParser(Clock clock2) {
        this.videos = new ArrayList();
        this.clock = clock2;
    }

    public boolean isVast1(byte[] input) {
        try {
            return isVast1(new String(input, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVast1(String input) {
        final boolean[] isVast1 = new boolean[1];
        DomDigester digester = new DomDigester();
        digester.addRule("VideoAdServingTemplate", new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                isVast1[0] = true;
            }
        });
        try {
            digester.parse(input);
            return isVast1[0];
        } catch (DomDigester.BadXmlException | IOException e) {
            return false;
        }
    }

    private void addDigesterRulesForVastVersion(final DomDigester digester) {
        this.vastVersion = -1;
        digester.addRule("VideoAdServingTemplate", new DomDigester.Rule() {
            public void executeBeforeChildren(Element element) {
                int unused = VastParser.this.vastVersion = 1;
            }
        });
        digester.addRule(BASE, new DomDigester.Rule() {
            public void executeBeforeChildren(Element element) {
                if (element.getAttribute("version") != null) {
                    String value = digester.getAttributeValue(element, "version").trim();
                    if (value.startsWith("2")) {
                        int unused = VastParser.this.vastVersion = 2;
                    } else if (value.startsWith("3")) {
                        int unused2 = VastParser.this.vastVersion = 3;
                    } else {
                        int unused3 = VastParser.this.vastVersion = -1;
                    }
                }
            }
        });
    }

    private void validate(VideoCreative.VastXml video, Source instanceDocument, String xsd) {
        try {
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource[]{new StreamSource(getClass().getResourceAsStream(xsd))}).newValidator().validate(instanceDocument);
        } catch (IOException e) {
            video.vastSchemaValidationErrors = e.getMessage();
        } catch (SAXException e2) {
            if (!e2.toString().contains("ddm/activity/dc_oe=") || !e2.toString().contains("anyURI")) {
                video.vastSchemaValidationErrors = e2.toString().replace("org.xml.sax.SAXParseException;", "").trim();
            }
        }
    }

    private String formatParserType(String s) {
        return formatParserType(s, false);
    }

    private String formatParserType(String s, boolean addUrlInVast1) {
        if (this.vastVersion == 1) {
            String str = "";
            String s2 = s.replace("VAST/", "VideoAdServingTemplate/").replace("/Creatives/Creative", str).replace("/VASTAdTagURI", "/VASTAdTagURL").replace("/StaticResource", str).replace("/HTMLResource", "/Code");
            if (s2.endsWith("/Tracking")) {
                s = s2.replace("/Linear", str);
            } else {
                if (this.parserType == ParserType.INLINE) {
                    str = "/Video";
                }
                s = s2.replace("/Linear", str);
            }
            if (addUrlInVast1) {
                s = s.concat("/URL");
            }
        }
        return String.format(s, this.parserType.toString());
    }

    private void addDigestorRulesForVastBody(DomDigester digester) {
        this.videos = new ArrayList();
        digester.push(this);
        this.parserType = ParserType.INLINE;
        digester.createObjectRule(formatParserType(BASE_AD), VideoCreative.VastXml.class);
        digester.setPropertiesRule(formatParserType(BASE_AD), TtmlNode.ATTR_ID, TtmlNode.ATTR_ID);
        digester.setPropertiesRule(formatParserType(BASE_AD), "sequence", "sequence");
        addRulesToDigester(digester, ParserType.INLINE);
        addRulesToDigester(digester, ParserType.WRAPPER);
        digester.addSetNext(formatParserType(BASE_AD), "addVideo");
    }

    private void addRulesToDigester(DomDigester digester, ParserType parserType2) {
        final DomDigester domDigester = digester;
        this.parserType = parserType2;
        domDigester.setPropertiesRule(formatParserType(CREATIVE), "AdID", "adId");
        domDigester.addRule(formatParserType(CREATIVE), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                VideoCreative.VastXml top = (VideoCreative.VastXml) domDigester.peek();
                Element linear = DomUtils.getFirstChildElement(element, VastParser.LINEAR);
                Element nonlinear = DomUtils.getFirstChildElement(element, VastParser.NONLINEAR_ELEMENT);
                if (linear != null || nonlinear != null) {
                    String id = domDigester.getAttributeValue(element, TtmlNode.ATTR_ID);
                    if (!TextUtils.isEmpty(id)) {
                        top.adId = id;
                    }
                }
            }
        });
        domDigester.setNodeValueRule(formatParserType(SURVEY), "survey");
        domDigester.createObjectRule(formatParserType(IMPRESSION, true), VideoCreative.VastImpression.class);
        domDigester.setNodeValueRule(formatParserType(IMPRESSION, true), "url");
        domDigester.setPropertiesRule(formatParserType(IMPRESSION, true), TtmlNode.ATTR_ID, TtmlNode.ATTR_ID);
        domDigester.addRule(formatParserType(IMPRESSION, true), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastXml) domDigester.belowPeek()).impression = new VideoCreative.VastImpression[]{(VideoCreative.VastImpression) domDigester.peek()};
            }
        });
        domDigester.setNodeValueRule(formatParserType(REDIRECT_URL, true), "redirectUrl");
        domDigester.addRule(formatParserType(DURATION), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastXml) domDigester.peek()).duration = VastParser.parseDuration(domDigester.getElementValue(element));
            }
        });
        domDigester.addRule(formatParserType(LINEAR_PARAMS), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastXml) domDigester.peek()).customParameters = domDigester.getElementValue(element);
            }
        });
        domDigester.addRule(formatParserType(EXTENSION), new DomDigester.Rule() {
            public void executeBeforeChildren(Element element) {
                VideoCreative.VastXml top = (VideoCreative.VastXml) domDigester.peek();
                if (element.getAttribute("type") != null) {
                    String type = element.getAttribute("type");
                    if ("waterfall".equals(type) && element.getAttribute("fallback_index") != null) {
                        top.fallbackIndex = VastParser.this.parseInteger(element.getAttribute("fallback_index"));
                    } else if ("pod".equals(type) && element.getAttribute("sequence") != null) {
                        top.sequence = VastParser.this.parseInteger(element.getAttribute("sequence"));
                    }
                }
            }
        });
        domDigester.addRule(formatParserType(EXTENSIONS_TRACKING_CUSTOM), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                VideoCreative.VastXml top = (VideoCreative.VastXml) domDigester.peek();
                String eventValue = element.getAttribute(NotificationCompat.CATEGORY_EVENT);
                if ("skip".equals(eventValue)) {
                    top.customSkipEventExists = true;
                }
                if ("viewable_impression".equals(eventValue)) {
                    VideoCreative.VastTracking vastTracking = new VideoCreative.VastTracking();
                    vastTracking.eventName = eventValue;
                    vastTracking.eventUrl = domDigester.getElementValue(element);
                    top.videoViewableImpression = vastTracking;
                }
            }
        });
        domDigester.setNodeValueRule(formatParserType(LINEAR_PARAMS), "customParameters");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(TRACKING_EVENTS), new String[]{"eventTracking"});
        domDigester.createObjectRule(formatParserType(TRACKING_EVENT), VideoCreative.VastTracking.class);
        domDigester.setNodeValueRule(formatParserType(TRACKING_EVENT, true), "eventUrl");
        domDigester.setPropertiesRule(formatParserType(TRACKING_EVENT), NotificationCompat.CATEGORY_EVENT, "eventName");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(TRACKING_EVENT, true), "eventTracking");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(VIDEO_CLICKS), new String[]{"clickTracking", "customTracking"});
        domDigester.addRule(formatParserType(DESTINATION_URL, true), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastXml) domDigester.belowPeek()).destinationUrl = domDigester.getElementValue(element);
            }
        });
        domDigester.createObjectRule(formatParserType(TRACKING_CLICK, true), VideoCreative.VastTracking.class);
        domDigester.setNodeValueRule(formatParserType(TRACKING_CLICK, true), "eventUrl");
        domDigester.setPropertiesRule(formatParserType(TRACKING_CLICK, true), TtmlNode.ATTR_ID, "eventName");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(TRACKING_CLICK, true), "clickTracking");
        domDigester.createObjectRule(formatParserType(TRACKING_CUSTOM, true), VideoCreative.VastTracking.class);
        domDigester.setNodeValueRule(formatParserType(TRACKING_CUSTOM, true), "eventUrl");
        domDigester.setPropertiesRule(formatParserType(TRACKING_CUSTOM, true), TtmlNode.ATTR_ID, "eventName");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(TRACKING_CUSTOM, true), "customTracking");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(MEDIA_FILES), new String[]{"media"});
        domDigester.createObjectRule(formatParserType(MEDIA_FILE), VideoCreative.VastMedia.class);
        domDigester.setNodeValueRule(formatParserType(MEDIA_FILE, true), "url");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "delivery", "delivery");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "type", "type");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "bitrate", "bitrate");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "width", "width");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "height", "height");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "scalable", "scalable");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "apiFramework", "apiFramework");
        domDigester.setPropertiesRule(formatParserType(MEDIA_FILE), "maintainAspectRatio", "maintainAspectRatio");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(MEDIA_FILE, true), "media");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(COMPANION_ADS), new String[]{"companion"});
        domDigester.createObjectRule(formatParserType(COMPANION), VideoCreative.VastCompanion.class);
        domDigester.setPropertiesRule(formatParserType(COMPANION), "width", "width");
        domDigester.setPropertiesRule(formatParserType(COMPANION), "height", "height");
        domDigester.setPropertiesRule(formatParserType(COMPANION), "expandedWidth", "expandedWidth");
        domDigester.setPropertiesRule(formatParserType(COMPANION), "expandedHeight", "expandedHeight");
        domDigester.setPropertiesRule(formatParserType(COMPANION), "apiFramework", "apiFramework");
        domDigester.setNodeValueRule(formatParserType("VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/CompanionClickThrough", true), "destinationUrl");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(COMPANION_TRACKING_EVENTS), new String[]{"eventTracking"});
        domDigester.createObjectRule(formatParserType(COMPANION_TRACKING_EVENT), VideoCreative.VastTracking.class);
        domDigester.setNodeValueRule(formatParserType(COMPANION_TRACKING_EVENT, true), "eventUrl");
        domDigester.setPropertiesRule(formatParserType(COMPANION_TRACKING_EVENT), NotificationCompat.CATEGORY_EVENT, "eventName");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(COMPANION_TRACKING_EVENT, true), "eventTracking");
        domDigester.setNodeValueRule(formatParserType("VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/HTMLResource"), "htmlResource");
        domDigester.setNodeValueRule(formatParserType("VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/IFrameResource"), "iframeResource");
        domDigester.setNodeValueRule(formatParserType("VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/StaticResource", true), "staticResource");
        domDigester.setPropertiesRule(formatParserType("VAST/Ad/%s/Creatives/Creative/CompanionAds/Companion/StaticResource"), "creativeType", "type");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(COMPANION, true), "companion");
        domDigester.collectAllChildrenResultIntoArrayFieldRule(formatParserType(NON_LINEAR_ADS), new String[]{"nonLinearAsset", "nonLinearEventTracking"});
        domDigester.createObjectRule(formatParserType(NON_LINEAR), VideoCreative.VastNonLinear.class);
        domDigester.setPropertiesRule(formatParserType(NON_LINEAR), "scalable", "scalable");
        domDigester.setPropertiesRule(formatParserType(NON_LINEAR), "maintainAspectRatio", "maintainAspectRatio");
        domDigester.addRule(formatParserType(NON_LINEAR), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                if (element.getAttribute("minSuggestedDuration") != null) {
                    ((VideoCreative.VastNonLinear) domDigester.peek()).minSuggestedDuration = VastParser.parseDuration(domDigester.getAttributeValue(element, "minSuggestedDuration"));
                }
            }
        });
        domDigester.collectChildResultIntoArrayListRule(formatParserType(NON_LINEAR, true), "nonLinearAsset");
        domDigester.createObjectRule(formatParserType(NON_LINEAR_AD_TRACKING_EVENT), VideoCreative.VastTracking.class);
        domDigester.setNodeValueRule(formatParserType(NON_LINEAR_AD_TRACKING_EVENT, true), "eventUrl");
        domDigester.setPropertiesRule(formatParserType(NON_LINEAR_AD_TRACKING_EVENT), NotificationCompat.CATEGORY_EVENT, "eventName");
        domDigester.collectChildResultIntoArrayListRule(formatParserType(NON_LINEAR_AD_TRACKING_EVENT, true), "nonLinearEventTracking");
        domDigester.addRule(formatParserType(NON_LINEAR_PARAMS), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastNonLinear) domDigester.peek()).customParameters = domDigester.getElementValue(element);
            }
        });
        domDigester.addRule(formatParserType(NON_LINEAR_CLICK_THROUGH), new DomDigester.Rule(this) {
            public void executeBeforeChildren(Element element) {
                ((VideoCreative.VastNonLinear) domDigester.peek()).destinationUrl = domDigester.getElementValue(element);
            }
        });
    }

    public AdConfig.AdAsset parse(String adUnitId, InputStream inputStream) {
        try {
            VideoCreative.VastXml vastXml = parse(getBytesFromInputStream(inputStream));
            AdConfig.AdAsset adAsset = new AdConfig.AdAsset();
            adAsset.expiration = getExpirationTimeInMillis();
            AdConfig.DoubleClickAdConfig doubleClickAdConfig = new AdConfig.DoubleClickAdConfig();
            adAsset.setDoubleclickAdConfig(doubleClickAdConfig);
            doubleClickAdConfig.adUnitId = adUnitId;
            doubleClickAdConfig.setVast(vastXml);
            return adAsset;
        } catch (DomDigester.BadXmlException | IOException ex) {
            Log.e(TAG, "Problem with vast ad format parsing..returning null", ex);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public VideoCreative.VastXml parse(byte[] input) throws DomDigester.BadXmlException {
        List<VideoCreative.VastXml> ads = parseMultiple(input);
        if (!ads.isEmpty()) {
            return ads.get(0);
        }
        VideoCreative.VastXml video = new VideoCreative.VastXml();
        video.vastVersion = this.vastVersion;
        return video;
    }

    private static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[65536];
        int len = inputStream.read(buffer);
        while (len != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
            len = inputStream.read(buffer);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private long getExpirationTimeInMillis() {
        return this.clock.getCurrentTimeMillis() + AD_VALIDITY_TTL_MS;
    }

    private List<VideoCreative.VastXml> parseMultiple(byte[] input) throws DomDigester.BadXmlException {
        DomDigester digester = new DomDigester();
        addDigesterRulesForVastVersion(digester);
        addDigestorRulesForVastBody(digester);
        try {
            digester.parse(new ByteArrayInputStream(input));
            return this.videos;
        } catch (IOException e) {
            throw new AssertionError("This exception cannot happen. Digester creates an InputSource and  passes it to the XML parser, however that InputSource cannot throw IOException", e);
        }
    }

    /* access modifiers changed from: private */
    public static int parseDuration(String videoDuration) {
        String[] tokens = videoDuration.split(":");
        if (tokens.length != 3 && tokens.length != 4) {
            return -1;
        }
        try {
            return (int) (TimeUnit.HOURS.toMillis((long) Integer.parseInt(tokens[0])) + TimeUnit.MINUTES.toMillis((long) Integer.parseInt(tokens[1])) + Math.round(Double.parseDouble(tokens[2]) * 1000.0d) + (tokens.length == 3 ? 0 : parseAndRoundNumber(tokens[3])));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static long parseAndRoundNumber(String number) throws NumberFormatException {
        return Math.round(Double.parseDouble(number));
    }

    /* access modifiers changed from: private */
    public int parseInteger(String number) {
        if (TextUtils.isEmpty(number)) {
            return 0;
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public VideoCreative.VastXml getVideo() {
        if (!this.videos.isEmpty()) {
            List<VideoCreative.VastXml> list = this.videos;
            return list.get(list.size() - 1);
        }
        VideoCreative.VastXml video = new VideoCreative.VastXml();
        video.vastVersion = this.vastVersion;
        return video;
    }

    public void addVideo(VideoCreative.VastXml video) {
        video.vastVersion = this.vastVersion;
        this.videos.add(video);
    }

    private static class SystemClock implements Clock {
        private SystemClock() {
        }

        public long getCurrentTimeMillis() {
            return System.currentTimeMillis();
        }

        public void sleep(long durationMillis) {
        }
    }
}

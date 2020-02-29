package android.support.p001v4.p003os;

import java.util.Locale;

/* renamed from: android.support.v4.os.LocaleListInterface */
interface LocaleListInterface {
    Locale get(int i);

    Locale getFirstMatch(String[] strArr);

    Object getLocaleList();

    int indexOf(Locale locale);

    boolean isEmpty();

    int size();

    String toLanguageTags();
}

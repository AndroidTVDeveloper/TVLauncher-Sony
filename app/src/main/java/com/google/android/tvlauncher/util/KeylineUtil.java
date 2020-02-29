package com.google.android.tvlauncher.util;

import androidx.leanback.widget.ItemAlignmentFacet;

public class KeylineUtil {
    public static ItemAlignmentFacet createItemAlignmentFacet(int offset) {
        return createItemAlignmentFacet(offset, null);
    }

    public static ItemAlignmentFacet createItemAlignmentFacet(int offset, Integer alignedItemResourceId) {
        ItemAlignmentFacet.ItemAlignmentDef def = new ItemAlignmentFacet.ItemAlignmentDef();
        def.setItemAlignmentOffset(offset);
        def.setItemAlignmentOffsetPercent(50.0f);
        if (alignedItemResourceId != null) {
            def.setItemAlignmentViewId(alignedItemResourceId.intValue());
        }
        ItemAlignmentFacet facet = new ItemAlignmentFacet();
        facet.setAlignmentDefs(new ItemAlignmentFacet.ItemAlignmentDef[]{def});
        return facet;
    }
}

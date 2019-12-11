// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.roots.ui.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.openapi.roots.ui.configuration.SdkListItem.*;

public class SdkListModel {
  private final boolean myIsSearching;
  private final ImmutableList<SdkListItem> myItems;
  private final ImmutableMap<SdkListItem, String> mySeparators;

  public SdkListModel(boolean isSearching, @NotNull List<? extends SdkListItem> items) {
    myIsSearching = isSearching;
    myItems = ImmutableList.copyOf(items);

    boolean myFirstSepSet = false;
    boolean mySuggestedSep = false;
    ImmutableMap.Builder<SdkListItem, String> sep = ImmutableMap.builder();

    for (SdkListItem it : myItems) {
      if (!myFirstSepSet && (it instanceof GroupItem || it instanceof ActionItem)) {
        myFirstSepSet = true;
        sep.put(it, "");
      }

      if (!mySuggestedSep && it instanceof SuggestedItem) {
        mySuggestedSep = true;
        sep.put(it, ProjectBundle.message("jdk.combo.box.autodetected"));
      }
    }
    mySeparators = sep.build();
  }

  @NotNull
  public SdkListModel buildSubModel(@NotNull GroupItem group) {
    return new SdkListModel(myIsSearching, group.mySubItems);
  }

  public boolean isSearching() {
    return myIsSearching;
  }

  @NotNull
  public List<SdkListItem> getItems() {
    return myItems;
  }

  @Nullable
  public String getSeparatorTextAbove(@NotNull SdkListItem value) {
    return mySeparators.get(value);
  }

  @Nullable
  public SdkItem findSdkItem(@NotNull Sdk value) {
    for (SdkListItem item : myItems) {
      if (!(item instanceof SdkItem)) continue;
      SdkItem sdkItem = (SdkItem)item;
      if (sdkItem.hasSameSdk(value)) {
        return sdkItem;
      }
    }
    return null;
  }
}

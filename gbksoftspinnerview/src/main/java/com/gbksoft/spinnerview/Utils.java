package com.gbksoft.spinnerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

final class Utils {

  static boolean isRtl(Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
        && context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  static Drawable getDrawable(Context context, int id) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return context.getDrawable(id);
    }
    return context.getResources().getDrawable(id);
  }
}

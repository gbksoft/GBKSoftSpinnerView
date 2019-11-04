package com.gbksoft.spinnerview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class SpinnerView extends AppCompatTextView {

    private OnNothingSelectedListener onNothingSelectedListener;
    private OnItemSelectedListener onItemSelectedListener;
    private SpinnerAdapter adapter;
    private PopupWindow dropDown;
    private ListView listView;
    private Drawable arrowDrawable;
    private Integer preferredItemsCount;
    private boolean adaptDropDownHeightToItemsSizeLimitedByMaxHeight;
    private boolean nothingSelected;
    private int dropDownMaxHeight;
    private int dropDownHeight;
    private int selectedIndex;

    public SpinnerView(Context context) {
        super(context);
        init(context, null);
    }

    public SpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SpinnerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpinnerView);

        int arrowColor;
        int popupWindowBackgroundId;
        int popupWindowElevation;
        try {
            arrowColor = ta.getColor(R.styleable.SpinnerView_sv_arrow_tint, Color.BLACK);
            dropDownMaxHeight = ta.getDimensionPixelSize(R.styleable.SpinnerView_sv_dropdown_max_height, 0);
            dropDownHeight = ta.getLayoutDimension(R.styleable.SpinnerView_sv_dropdown_height, WindowManager.LayoutParams.WRAP_CONTENT);
            arrowDrawable = ta.getDrawable(R.styleable.SpinnerView_sv_arrow_drawable);
            popupWindowBackgroundId = ta.getResourceId(R.styleable.SpinnerView_sv_dropdown_background, 0);
            popupWindowElevation = ta.getDimensionPixelSize(R.styleable.SpinnerView_sv_dropdown_elevation, 16);
            int tempDisplayedItemsCount = ta.getInteger(R.styleable.SpinnerView_sv_dropdown_displayed_items_count, -1);
            preferredItemsCount = tempDisplayedItemsCount > 0 ? tempDisplayedItemsCount : null;
            adaptDropDownHeightToItemsSizeLimitedByMaxHeight = ta.getBoolean(R.styleable.SpinnerView_sv_dropdown_adapt_height_to_items_size, false);
        } finally {
            ta.recycle();
        }
        selectedIndex = -1;
        setClickable(true);

        boolean rtl = Utils.isRtl(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && rtl) {
            setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            setTextDirection(View.TEXT_DIRECTION_RTL);
        }

        if (arrowDrawable != null) {
            arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = getCompoundDrawables();
            if (rtl) {
                drawables[0] = arrowDrawable;
            } else {
                drawables[2] = arrowDrawable;
            }
            setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nothingSelected = false;
                String itemString = adapter.getItemString(position);
                setText(itemString);
                collapse();
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            }
        });

        dropDown = new PopupWindow(context);
        dropDown.setContentView(listView);
        dropDown.setOutsideTouchable(true);
        dropDown.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dropDown.setElevation(popupWindowElevation);
        }

        if (popupWindowBackgroundId != 0) {
            dropDown.setBackgroundDrawable(Utils.getDrawable(context, popupWindowBackgroundId));
        }

        dropDown.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                if (nothingSelected && onNothingSelectedListener != null) {
                    onNothingSelectedListener.onNothingSelected();
                }
                animateArrow(false);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        dropDown.setWidth(MeasureSpec.getSize(widthMeasureSpec));
        dropDown.setHeight(calculatePopupWindowHeight());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isEnabled() && isClickable()) {
                if (dropDown.isShowing()) {
                    collapse();
                } else {
                    expand();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int position) {
        if (adapter != null) {
            if (position >= 0 && position <= adapter.getCount()) {
                selectedIndex = position;
                setText(adapter.getItem(position).toString());
            }
        }
    }

    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public void setOnNothingSelectedListener(@Nullable OnNothingSelectedListener onNothingSelectedListener) {
        this.onNothingSelectedListener = onNothingSelectedListener;
    }

    public void setAdapter(@NonNull SpinnerAdapter adapter) {
        this.adapter = adapter;
        boolean shouldResetPopupHeight = listView.getAdapter() != null;
        listView.setAdapter(adapter);
        if (TextUtils.isEmpty(getHint()) && adapter.getCount() > 0) {
            selectedIndex = 0;
            setText(adapter.getItem(selectedIndex).toString());
        } else {
            selectedIndex = -1;
            setText(null);
        }
        if (shouldResetPopupHeight) {
            dropDown.setHeight(calculatePopupWindowHeight());
        }
    }

    public void expand() {
        animateArrow(true);
        nothingSelected = true;
        dropDown.showAsDropDown(this);
    }

    public void collapse() {
        animateArrow(false);
        dropDown.dismiss();
    }

    private void animateArrow(boolean shouldRotateUp) {
        if (arrowDrawable == null) return;
        int start = shouldRotateUp ? 0 : 10000;
        int end = shouldRotateUp ? 10000 : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
        animator.start();
    }

    private int calculatePopupWindowHeight() {
        if (adapter != null && adapter.getCount() > 0) {
            float listViewHeight;
            if(preferredItemsCount != null){
                int itemsToCalculate = preferredItemsCount > adapter.getCount() ? adapter.getCount() : preferredItemsCount;
                listViewHeight = getItemHeightForItems(itemsToCalculate);
                if(dropDownMaxHeight <= 0){
                    int possibleHeight = (int) listViewHeight;
                    if(dropDown != null) {
                        int availableHeight = dropDown.getMaxAvailableHeight(this);
                        if (availableHeight > 0 && availableHeight < possibleHeight) {
                            possibleHeight = availableHeight;
                        }
                    }
                    return possibleHeight;
                }
            }else{
                listViewHeight = getListViewHeight();
            }
            if (dropDownMaxHeight > 0 && listViewHeight > dropDownMaxHeight) {
                if (adaptDropDownHeightToItemsSizeLimitedByMaxHeight) {
                    return adaptDropDownMaxHeightToMatchItemsInList(dropDownMaxHeight);
                } else {
                    return dropDownMaxHeight;
                }

            } else {
                if(dropDown != null && adaptDropDownHeightToItemsSizeLimitedByMaxHeight){
                    return adaptDropDownMaxHeightToMatchItemsInList(dropDown.getMaxAvailableHeight(this));
                }
                return dropDownHeight;
            }

        }
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private int adaptDropDownMaxHeightToMatchItemsInList(int limit) {
        int resultMaxHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            int currentItemHeight = getItemHeight(i);
            int tempResult = resultMaxHeight + currentItemHeight;
            if (tempResult > limit) {
                return resultMaxHeight;
            } else {
                resultMaxHeight += currentItemHeight;
            }
        }
        return limit;
    }

    private int getItemHeight(int position) {
        View item = adapter.getView(position, null, listView);
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        item.measure(measureSpec, measureSpec);
        return item.getMeasuredHeight();
    }

    private int getListViewHeight() {
        return getItemHeightForItems(adapter.getCount());
    }

    private int getItemHeightForItems(int itemsCount) {
        int resultViewHeight = 0;
        for (int i = 0; i < itemsCount; i++) {
            resultViewHeight += getItemHeight(i);
        }
        return resultViewHeight;
    }

    public PopupWindow getDropDown() {
        return dropDown;
    }

    public ListView getListView() {
        return listView;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    public interface OnNothingSelectedListener {
        void onNothingSelected();
    }
}
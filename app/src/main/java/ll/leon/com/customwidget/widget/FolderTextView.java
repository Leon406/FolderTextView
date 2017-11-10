package ll.leon.com.customwidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

import ll.leon.com.customwidget.R;


/**
 * Author : Leon
 * E-mail : deadogone@gmail.com
 * Time   : 2017/9/21 0021 15:26
 * Desc   : This is FolderTextView
 * Version: 1.0.1
 */

public class FolderTextView extends android.support.v7.widget.AppCompatTextView {

    private static final String ELLIPSIS = "...";
    private static final String FOLD_TEXT = "  折叠";  //展开文本
    private static final String UNFOLD_TEXT = "  全部";  //折叠文本

    /**
     * 收缩状态
     */
    private boolean isFold = false;
    private boolean isLess = false;
    private boolean noFold = true;  //不能折叠  false 能折叠

    /**
     * 绘制，防止重复进行绘制
     */
    private boolean isDrawed = false;
    ClickableSpan clickSpan = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            isFold = !isFold;
            isDrawed = false;
            invalidate();
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
        }
    };
    /**
     * 内部绘制
     */
    private boolean isInner = false;
    /**
     * 折叠行数
     */
    private int foldLine;
    /**
     * 全文本
     */
    private String fullText;
    private float mSpacingMult = 1.0f;
    private float mSpacingAdd = 0.0f;
    private static final String TAGView = "FolderTextView";
    private String fold_txt;
    private String unFold_txt;
    private int unFold_color;
    private int fold_color;

    public FolderTextView(Context context) {
        this(context, null);
    }

    public FolderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //自定义属性解析
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FolderTextView);
        foldLine = a.getInt(R.styleable.FolderTextView_fold_line, 2);
        noFold = a.getBoolean(R.styleable.FolderTextView_noFold, false);
        fold_txt = a.getString(R.styleable.FolderTextView_fold_text);

        if (TextUtils.isEmpty(fold_txt)) {
            fold_txt = FOLD_TEXT;
        }

        unFold_txt = a.getString(R.styleable.FolderTextView_unfold_text);
        if (TextUtils.isEmpty(unFold_txt)) {
            unFold_txt = UNFOLD_TEXT;
        }
        unFold_color = a.getColor(R.styleable.FolderTextView_unfold_text_color, 0xffFE026C);
        fold_color = a.getColor(R.styleable.FolderTextView_fold_text_color, 0xff3F51B5);
        a.recycle();
    }

    /**
     * 不更新全文本下，进行展开和收缩操作
     *
     * @param text
     */
    private void setUpdateText(CharSequence text) {
        isInner = true;
        setText(text);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(fullText) || !isInner) {
            isDrawed = false;
            fullText = String.valueOf(text);
        }
        super.setText(text, type);
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        mSpacingAdd = add;
        mSpacingMult = mult;
        super.setLineSpacing(add, mult);
    }

    public int getFoldLine() {
        return foldLine;
    }

    public void setFoldLine(int foldLine) {
        this.foldLine = foldLine;
    }

    private Layout makeTextLayout(String text) {
        return new StaticLayout(text, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
                Layout.Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isDrawed) {
            resetText();
        }
        super.onDraw(canvas);
        isDrawed = true;
        isInner = false;
    }

    private void resetText() {
        String spanText = fullText;

        SpannableString spanStr;

        //收缩状态
        if (isFold) {
            spanStr = createUnFoldSpan(spanText);
        } else { //展开状态
            spanStr = createFoldSpan(spanText);
        }
        if (isLess) {
            setUpdateText(fullText);
        } else {
            setUpdateText(spanStr);
            setMovementMethod(LinkMovementMethod.getInstance());
        }


    }

    /**
     * 创建展开状态下的Span
     *
     * @param text 源文本
     * @return
     */
    private SpannableString createUnFoldSpan(String text) {
        String destStr = text + fold_txt;

        int start;
        int end;
        if (noFold) {
            start = 0;
            end = 0;
            destStr = text;
        } else {
            start = destStr.length() - fold_txt.length();
            end = destStr.length();
        }

        SpannableString spanStr = new SpannableString(destStr);
        spanStr.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(fold_color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 创建收缩状态下的Span
     *
     * @param text
     * @return
     */
    private SpannableString createFoldSpan(String text) {
        String destStr = "";
        Layout layout = makeTextLayout(text);
        if (layout.getLineCount() <= getFoldLine()) {
            destStr = text;
            isLess = true;
        } else {
            destStr = tailorText(text);
        }

        int start;
        int end;
        if (isLess) {
            start = 0;
            end = 0;
        } else {
            start = destStr.length() - unFold_txt.length();
            end = destStr.length();
        }

        SpannableString spanStr = new SpannableString(destStr);


        spanStr.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(unFold_color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    /**
     * 裁剪文本至固定行数
     *
     * @param text 源文本
     * @return
     */
    private String tailorText(String text) {


        String destStrTemp = text + unFold_txt;
        Layout layout = makeTextLayout(destStrTemp);

        if (layout.getLineCount() < getFoldLine()) {
            isLess = true;
            return text;
        } else {
            isLess = false;
        }
        String destStr = text + ELLIPSIS + unFold_txt;
        //如果行数大于固定行数
        if (layout.getLineCount() > getFoldLine()) {
            int index = layout.getLineEnd(getFoldLine());
            if (text.length() < index) {
                index = text.length();
            }
            String subText = text.substring(0, index - 1); //从最后一位逐渐试错至固定行数
            return tailorText(subText);
        } else {
            return destStr.substring(0, destStr.length() - ELLIPSIS.length() - unFold_txt.length() - 2) + destStr.substring(destStr.length() - ELLIPSIS.length() - unFold_txt.length());
        }
    }
}


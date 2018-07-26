package com.jrdcom.filemanager.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.jrdcom.filemanager.R;
import com.jrdcom.filemanager.utils.CommonIdentity;
import com.jrdcom.filemanager.utils.CommonUtils;
import com.jrdcom.filemanager.view.ToastHelper;

import java.io.UnsupportedEncodingException;

public class AlertDialogFragment extends DialogFragment implements
        OnClickListener {
    public static final String TAG = "AlertDialogFragment";

    private static final String TITLE = "title";
    private static final String CANCELABLE = "cancelable";
    private static final String ICON = "icon";
    private static final String MESSAGE = "message";
    private static final String LAYOUT = "layout";
    private static final String NEGATIVE_TITLE = "negativeTitle";
    private static final String POSITIVE_TITLE = "positiveTitle";

    public static final int INVIND_RES_ID = -1;

    protected static OnClickListener mDoneListener;
    protected OnDismissListener mDismissListener;
    protected ToastHelper mToastHelper;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(getArguments());
        super.onSaveInstanceState(outState);
    }

    public static class AlertDialogFragmentBuilder {
        protected final Bundle mBundle = new Bundle();

        /**
         * This method creates AlertDialogFragment with parameter of mBundle.
         *
         * @return AlertDialogFragment
         */
        public AlertDialogFragment create() {
            AlertDialogFragment f = new AlertDialogFragment();
            f.setArguments(mBundle);
            return f;
        }

        /**
         * This method sets TITLE for AlertDialogFragmentBuilder, which responds
         * to title of dialog.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setTitle(int resId) {
            mBundle.putInt(TITLE, resId);
            return this;
        }

        /**
         * This method sets LAYOUT for AlertDialogFragmentBuilder, which
         * responds to layout of dialog.
         *
         * @param resId resource id of layout
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setLayout(int resId) {
            mBundle.putInt(LAYOUT, resId);
            return this;
        }

        /**
         * This method sets CANCELABLE for AlertDialogFragmentBuilder (default
         * value is true), which responds to weather dialog can be canceled.
         *
         * @param cancelable true for can be canceled, and false for can not be
         *                   canceled
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setCancelable(boolean cancelable) {
            mBundle.putBoolean(CANCELABLE, cancelable);
            return this;
        }

        /**
         * This method sets ICON for AlertDialogFragmentBuilder.
         *
         * @param resId resource id of icon
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setIcon(int resId) {
            mBundle.putInt(ICON, resId);
            return this;
        }

        /**
         * This method sets MESSAGE for AlertDialogFragmentBuilder, which is a
         * string.
         *
         * @param resId resource id of message
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setMessage(int resId) {
            mBundle.putInt(MESSAGE, resId);
            return this;
        }

        /**
         * This method sets NEGATIVE_TITLE for AlertDialogFragmentBuilder, which
         * responds to title of negative button.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setCancelTitle(int resId) {
            mBundle.putInt(NEGATIVE_TITLE, resId);
            return this;
        }

        /**
         * This method sets POSITIVE_TITLE for AlertDialogFragmentBuilder, which
         * responds to title of positive button.
         *
         * @param resId resource id of title
         * @return AlertDialogFragmentBuilder
         */
        public AlertDialogFragmentBuilder setDoneTitle(int resId) {
            mBundle.putInt(POSITIVE_TITLE, resId);
            return this;
        }
    }

    /**
     * This method sets doneListenser for AlertDialogFragment
     *
     * @param listener doneListenser for AlertDialogFragment, which will
     *                 response to press done button
     */
    public void setOnDoneListener(OnClickListener listener) {
        mDoneListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDoneListener != null) {
            mDoneListener.onClick(dialog, which);
            mDoneListener = null;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = createAlertDialogBuilder(savedInstanceState);
        return builder.create();
    }

    /**
     * This method gets a instance of AlertDialog.Builder
     *
     * @param savedInstanceState information for AlertDialog.Builder
     * @return
     */
    protected Builder createAlertDialogBuilder(Bundle savedInstanceState) {
        Bundle args = null;
        if (savedInstanceState == null) {
            args = getArguments();
        } else {
            args = savedInstanceState;
        }
        Builder builder = new Builder(getActivity());
        if (args != null) {
            int title = args.getInt(TITLE, INVIND_RES_ID);
            if (title != INVIND_RES_ID) {
                builder.setTitle(title);
            }

            int icon = args.getInt(ICON, INVIND_RES_ID);
            if (icon != INVIND_RES_ID) {
                builder.setIcon(icon);
            }

            int message = args.getInt(MESSAGE, INVIND_RES_ID);
            int layout = args.getInt(LAYOUT, INVIND_RES_ID);
            if (layout != INVIND_RES_ID) {
                View view = getActivity().getLayoutInflater().inflate(layout,
                        null);
                builder.setView(view);
            } else if (message != INVIND_RES_ID) {
                builder.setMessage(message);
            }

            int cancel = args.getInt(NEGATIVE_TITLE, INVIND_RES_ID);

            if (cancel != INVIND_RES_ID) {
                builder.setNegativeButton(cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mDoneListener = null;
                        EditTextDialogFragment.mEditTextDoneListener = null;
                    }
                });
            }

            int done = args.getInt(POSITIVE_TITLE, INVIND_RES_ID);
            if (done != INVIND_RES_ID) {
                builder.setPositiveButton(done, this);
            }

            mToastHelper = new ToastHelper(getActivity());
            boolean cancelable = args.getBoolean(CANCELABLE, true);
            builder.setCancelable(cancelable);
        }
        return builder;
    }

    /**
     * This method sets dismissListener for AlertDialogFragment, which will
     * response to dismissDialog
     *
     * @param listener OnDismissListener for AlertDialogFragment
     */
    public void setDismissListener(OnDismissListener listener) {
        mDismissListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mDismissListener != null) {
            mDismissListener.onDismiss(dialog);
        }
        super.onDismiss(dialog);
    }

    public static class EditDialogFragmentBuilder extends
            AlertDialogFragmentBuilder {
        @Override
        public EditTextDialogFragment create() {
            EditTextDialogFragment f = new EditTextDialogFragment();
            f.setArguments(mBundle);
            return f;
        }

        /**
         * This method sets default string and default selection for
         * EditTextDialogFragment.
         *
         * @param defaultString    default string to show on EditTextDialogFragment
         * @param defaultSelection resource id for default selection
         * @return EditDialogFragmentBuilder
         */
        public EditDialogFragmentBuilder setDefault(String defaultString,
                                                    int defaultSelection, boolean hint) {
            mBundle.putString(EditTextDialogFragment.DEFAULT_STRING,
                    defaultString);
            mBundle.putInt(EditTextDialogFragment.DEFAULT_SELCTION,
                    defaultSelection);
            mBundle.putBoolean(EditTextDialogFragment.DEFAULT_HINT, hint);
            return this;
        }
    }

    public static class EditTextDialogFragment extends AlertDialogFragment {
        public static final String TAG = "EditTextDialogFragment";
        public static final String DEFAULT_STRING = "defaultString";
        public static final String DEFAULT_SELCTION = "defaultSelection";
        public static final String DEFAULT_HINT = "defaultHint";
        private EditText mEditText;
        protected static EditTextDoneListener mEditTextDoneListener;
        boolean mHasToasted = false;
        boolean isHighN = false;

        public interface EditTextDoneListener {
            /**
             * This method is used to overwrite by its implement
             *
             * @param text text on EditText when done button is pressed
             */
            void onClick(String text);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            getArguments().putString(DEFAULT_STRING,
                    mEditText.getText().toString());
            getArguments().putInt(DEFAULT_SELCTION,
                    mEditText.getSelectionStart());
            super.onSaveInstanceState(outState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            this.setOnDoneListener(this);
            Builder builder = createAlertDialogBuilder(savedInstanceState);
            Bundle args = null;
            isHighN = CommonUtils.hasHighN();
            if (savedInstanceState == null) {
                args = getArguments();
            } else {
                args = savedInstanceState;
            }
            if (args != null) {
                String defaultString = args.getString(DEFAULT_STRING, "");
                int selection = args.getInt(DEFAULT_SELCTION, 0);
                boolean hint = args.getBoolean(DEFAULT_HINT, false);
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_text, null);
                builder.setView(view);
                mEditText = (EditText) view.findViewById(R.id.edit_text);
                if (hint) {
                    mEditText.setHint(defaultString);
                } else {
                    mEditText.setText(defaultString);
                    if (selection >= defaultString.length()) {
                        selection = defaultString.length();
                    }
                    if (selection < 0) {
                        selection = 0;
                    }
                    if (selection >= getResources().getInteger(R.integer.name_max_length)) {
                        selection = getResources().getInteger(R.integer.name_max_length);
                    }
                    mEditText.setSelection(selection);
                }
            }
            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mEditText != null && mEditText.getText().length() == 0) {
                final Button button = ((AlertDialog) getDialog())
                        .getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(false);
                }
            }
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            setTextChangedCallback(mEditText, (AlertDialog) getDialog());
        }

        /**
         * The method is used to set filter to EditText which is used for user
         * entering filename. This filter will ensure that the inputed filename
         * wouldn't be too long. If so, the inputed info would be rejected.
         *
         * @param edit      The EditText for filter to be registered.
         * @param maxLength limitation of length for input text
         */
        private void setEditTextFilter(final EditText edit, final int maxLength) {
            InputFilter filter = new InputFilter.LengthFilter(maxLength) {
                private static final int VIBRATOR_TIME = 100;
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    boolean isTooLong = false;
                    try {
                        int length = dest.toString().getBytes("UTF-8").length - (dend - dstart) + (end - start);
                        if (maxLength - length < 0) {
                            isTooLong = true;
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    if (isTooLong) {
                        Vibrator vibrator = (Vibrator) getActivity()
                                .getSystemService(Context.VIBRATOR_SERVICE);
                        boolean hasVibrator = vibrator.hasVibrator();
                        if (hasVibrator) {
                            vibrator.vibrate(new long[]{VIBRATOR_TIME,
                                    VIBRATOR_TIME}, INVIND_RES_ID);
                        }
                        try {
                            mToastHelper.showToast(R.string.file_name_too_long);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (source != null && source.length() > 0 && !mHasToasted
                            && dstart == 0) {
                        if (source.charAt(0) == '.') {
                            mToastHelper.showToast(R.string.create_hidden_file);
                            mHasToasted = true;
                        }
                    }
                    return super.filter(source, start, end, dest, dstart, dend);

                }
            };
            edit.setFilters(new InputFilter[]{filter});
        }

        /**
         * This method register callback and set filter to Edit, in order to
         * make sure that user input is legal. The input can't be illegal
         * filename and can't be too long.
         *
         * @param editText EditText, which user type on
         * @param dialog   dialog, which EditText associated with
         */
        protected void setTextChangedCallback(final EditText editText, final AlertDialog dialog) {
            setEditTextFilter(editText, CommonIdentity.FILENAME_MAX_LENGTH);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    String input = s.toString().trim().replace("\n", "");
                    if (TextUtils.isEmpty(input) || input.matches(".*[/\\\\:*?\"<>|].*")) {
                        if (input.length() > 0 && input.matches(".*[/\\\\:*?\"<>|].*")) {
                            mToastHelper.showToast(R.string.invalid_char_prompt);
                        }

                        Button botton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (botton != null) {
                            botton.setEnabled(false);
                        }
                    } else {
                        if (input.trim().equalsIgnoreCase("..") || input.trim().equalsIgnoreCase(".")) {
                            Button botton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if (botton != null) {
                                botton.setEnabled(false);
                            }
                        } else {
                            Button botton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            if (botton != null) {
                                botton.setEnabled(true);
                            }
                        }
                    }
                }
            });
        }

        /**
         * This method gets EditText's content on EditTextDialogFragment
         *
         * @return content of EditText
         */
        public String getText() {
            if (mEditText != null) {
                return mEditText.getText().toString().trim();
            }
            return null;
        }

        /**
         * This method sets EditTextDoneListener for EditTextDialogFragment
         *
         * @param listener EditTextDoneListener, which will response press done
         *                 button
         */
        public void setOnEditTextDoneListener(EditTextDoneListener listener) {
            mEditTextDoneListener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mEditTextDoneListener != null) {
                mEditTextDoneListener.onClick(getText());
                mEditTextDoneListener = null;
            }
        }
    }

}
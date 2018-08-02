package jp.co.recruit_lifestyle.sample.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.recruit.floatingview.R;
import jp.co.recruit_lifestyle.sample.service.ChatHeadService;
import jp.co.recruit_lifestyle.sample.service.CustomFloatingViewService;


/**
 * FloatingViewのメイン画面となるフラグメントです。
 */
public class FloatingViewControlFragment extends Fragment {

    /**
     * デバッグログ用のタグ
     */
    private static final String TAG = "FloatingViewControl";

    /**
     * シンプルなFloatingViewを表示するフローのパーミッション許可コード
     */
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    /**
     * カスタマイズFloatingViewを表示するフローのパーミッション許可コード
     */
    private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;

    /**
     * FloatingViewControlFragmentを生成します。
     */
    public static FloatingViewControlFragment newInstance() {
        final FloatingViewControlFragment fragment = new FloatingViewControlFragment();
        return fragment;
    }

    /**
     * コンストラクタ
     */
    public FloatingViewControlFragment() {
        // Required empty public constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_floating_view_control, container, false);
        // デモの表示
        rootView.findViewById(R.id.show_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingView(getActivity(), true, false);
            }
        });
        // カスタマイズデモの表示
        rootView.findViewById(R.id.show_customized_demo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloatingView(getActivity(), true, true);
            }
        });
        // 設定画面の表示
        rootView.findViewById(R.id.show_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, FloatingViewSettingsFragment.newInstance());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return rootView;
    }

    /**
     * オーバレイ表示の許可を処理します。
     */
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(getActivity(), false, false);
        } else if (requestCode == CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(getActivity(), false, true);
        }
    }

    /**
     * FloatingViewの表示
     *
     * @param context                 Context
     * @param isShowOverlayPermission 表示できなかった場合に表示許可の画面を表示するフラグ
     * @param isCustomFloatingView    If true, it launches CustomFloatingViewService.
     */
    @SuppressLint("NewApi")
    private void showFloatingView(Context context, boolean isShowOverlayPermission, boolean isCustomFloatingView) {
        // API22以下かチェック
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService(getActivity(), isCustomFloatingView);
            return;
        }

        // 他のアプリの上に表示できるかチェック
        if (Settings.canDrawOverlays(context)) {
            startFloatingViewService(getActivity(), isCustomFloatingView);
            return;
        }

        // オーバレイパーミッションの表示
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Start floating view service
     *
     * @param activity             {@link Activity}
     * @param isCustomFloatingView If true, it launches CustomFloatingViewService.
     */
    private static void startFloatingViewService(Activity activity, boolean isCustomFloatingView) {
        // set safe inset area
        final Rect safeInsetRect = new Rect();
        // TODO:Rewrite with android-x
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final DisplayCutout displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
            if (displayCutout != null) {
                safeInsetRect.set(displayCutout.getSafeInsetLeft(), displayCutout.getSafeInsetTop(), displayCutout.getSafeInsetRight(), displayCutout.getSafeInsetBottom());
            }
        }

        // launch service
        if (isCustomFloatingView) {
            final Intent intent = new Intent(activity, CustomFloatingViewService.class);
            intent.putExtra(CustomFloatingViewService.EXTRA_CUTOUT_SAFE_AREA, safeInsetRect);
            ContextCompat.startForegroundService(activity, intent);
        } else {
            final Intent intent = new Intent(activity, ChatHeadService.class);
            intent.putExtra(ChatHeadService.EXTRA_CUTOUT_SAFE_AREA, safeInsetRect);
            ContextCompat.startForegroundService(activity, intent);
        }
    }
}
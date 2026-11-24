package io.github.snavprojects.browser;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.provider.MediaStore;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.PageExtractionController;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;
import org.mozilla.geckoview.WebRequest;
import org.mozilla.geckoview.GeckoWebExecutor;
import android.widget.*;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import org.mozilla.geckoview.TranslationsController;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private double toque = 0;
	private GeckoEngineController geckoEngine;
	private int geckoScrollY;
	private boolean javaScriptEnabled = true;
	private boolean readerMode;
	private boolean readerModePending;
	private int readerRequestId;
	private String readerOriginalUri;
	private String currentPageTitle;
	private View readerContentView;
	private volatile String detectedPageLanguage;
	private HandlerThread translationThread;
	private Handler translationHandler;
	private GeckoSession.SessionState pendingSessionState;
	private String pendingRestoreUri;
	private static final String STATE_GECKO_SESSION = "snav_gecko_session_state";
	private static final String STATE_GECKO_URI = "snav_gecko_current_uri";
	private GeckoSession.PermissionDelegate.Callback pendingAndroidPermissionCallback;
	private String pendingAndroidPermissionOrigin;
	private String[] pendingAndroidPermissions;
    private final java.util.List<GeckoResult<Integer>> pendingContentPermissions = new java.util.ArrayList<>();
    private GeckoResult<Integer> pendingNotificationPermission;
    private String pendingNotificationOrigin;
    private boolean pendingDownloadNotificationPermission;
	private GeckoSession.PermissionDelegate.MediaCallback pendingMediaPermissionCallback;
	private GeckoSession.PermissionDelegate.MediaSource[] pendingVideoSources;
	private GeckoSession.PermissionDelegate.MediaSource[] pendingAudioSources;
	private String pendingMediaPermissionOrigin;
	private WebResponse pendingGeckoDownload;
	private String pendingGeckoDownloadName;
	private final java.util.concurrent.ExecutorService downloadExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private static final java.util.concurrent.atomic.AtomicInteger DOWNLOAD_IDS = new java.util.concurrent.atomic.AtomicInteger(7000);
    private static final java.util.concurrent.ConcurrentHashMap<Integer, DownloadStatus> ACTIVE_DOWNLOADS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final String DOWNLOAD_CHANNEL = "snav_downloads";
    private static final class DownloadStatus {
        final String name;
        final long total;
        volatile long bytes;
        DownloadStatus(String name, long total) { this.name = name; this.total = total; }
    }
	
	private LinearLayout linear1;
	private LinearLayout tela1;
	private LinearLayout tela2;
	private LinearLayout tela3;
	private TextView text_icon;
	private LinearLayout linear_barra_pesquisa;
	private EditText edittext1;
	private LinearLayout linear20;
	private LinearLayout linear_b;
	private LinearLayout vazio;
	private GeckoView geckoView;
	private FrameLayout browserContentContainer;
    private RefreshComposeHost refreshHost;
	private TextView text_barra;
	private ImageButton button_site_info;
	private ImageButton button_menu;
	private ProgressBar prog;
	private TextView text1;
	private TextView text2;
	private ScrollView vscroll1;
	private LinearLayout linear33;
	private LinearLayout linear_tela3_base;

	private LinearLayout linear_escurecer;
	private LinearLayout linear_JavaScript;
	private LinearLayout linear_limpar_dados2;

	private MaterialSwitch checkbox_brilho;
	private MaterialSwitch checkbox_wallpaper;
    private LinearLayout homeShortcuts;
    private View pageErrorView;
    private String failedPageUrl;
    private String loadingPageUrl;
    private boolean stoppedByUser;
    private boolean toolsOpenedFromBrowser;
    private final java.util.concurrent.ExecutorService faviconExecutor = java.util.concurrent.Executors.newFixedThreadPool(2);
    private final android.util.LruCache<String, Bitmap> faviconCache = new android.util.LruCache<>(24);
    private final Set<String> faviconRequests = new HashSet<>();
    private final Set<String> faviconAttempted = new HashSet<>();
	private UnsplashWallpaperManager wallpaperManager;
	private UnsplashWallpaperManager.Callback wallpaperCallback;
    private ExtensionManager extensionManager;
    private Uri pendingExtensionFile;
	private MaterialSwitch checkbox_javascript2;
	private TextView text5;
	
	private SharedPreferences Dados;
	private BrowserLibraryStore browserLibraryStore;
	private final ArrayList<BrowserTab> browserTabs = new ArrayList<>();
	private int activeBrowserTab = 0;
	private TextView tabCounterButton;
	private static final String STATE_BROWSER_TABS = "snav_browser_tabs";
	private static final String STATE_ACTIVE_TAB = "snav_active_tab";
	private TimerTask time;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
        if (getSharedPreferences("Dados", MODE_PRIVATE).getBoolean("dynamic_color", true)) {
            com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
        }
        int background = themeColor(android.R.attr.colorBackground, R.color.snav_background);
        int surface = themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface);
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(surface);
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags = androidx.core.graphics.ColorUtils.calculateLuminance(background) > 0.5
            ? flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        flags = androidx.core.graphics.ColorUtils.calculateLuminance(surface) > 0.5
            ? flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
		setContentView(R.layout.main);
		applyImeInsetsWorkaround();
		if (_savedInstanceState != null) {
			if (Build.VERSION.SDK_INT >= 33) {
				pendingSessionState = _savedInstanceState.getParcelable(STATE_GECKO_SESSION, GeckoSession.SessionState.class);
			} else {
				pendingSessionState = (GeckoSession.SessionState) _savedInstanceState.getParcelable(STATE_GECKO_SESSION);
			}
			pendingRestoreUri = _savedInstanceState.getString(STATE_GECKO_URI);
			ArrayList<Bundle> tabBundles = _savedInstanceState.getParcelableArrayList(STATE_BROWSER_TABS);
			if (tabBundles != null) {
				for (Bundle item : tabBundles) {
					GeckoSession.SessionState state = Build.VERSION.SDK_INT >= 33 ? item.getParcelable("state", GeckoSession.SessionState.class) : (GeckoSession.SessionState) item.getParcelable("state");
					browserTabs.add(new BrowserTab(item.getString("title"), item.getString("url"), state));
				}
				if (!browserTabs.isEmpty()) activeBrowserTab = Math.max(0, Math.min(_savedInstanceState.getInt(STATE_ACTIVE_TAB, 0), browserTabs.size() - 1));
			}
		}
		startDiagnostics();
		initialize(_savedInstanceState);
		initializeLogic();
        setupAdaptiveWidths();
        if (_savedInstanceState != null && _savedInstanceState.containsKey("snav_visible_screen")) {
            toolsOpenedFromBrowser = _savedInstanceState.getBoolean("snav_tools_from_browser", false);
            showScreen(_savedInstanceState.getInt("snav_visible_screen", 1));
        }
		addDiagnosticMenuAction();
		showPendingCrashReport();
        if (getIntent() != null && getIntent().getBooleanExtra("snav_show_downloads", false)) {
            getIntent().removeExtra("snav_show_downloads");
            findViewById(R.id.snav_snackbar_host).post(this::showDownloadsBottomSheet);
        }
	}

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.getBooleanExtra("snav_show_downloads", false)) {
            intent.removeExtra("snav_show_downloads");
            showDownloadsBottomSheet();
        }
    }
	
	/**
	 * Android 15 enforces edge-to-edge for apps targeting SDK 35. In that mode
	 * adjustResize alone may leave the content drawing behind the IME. Apply the
	 * IME/system-bar insets to the content host so the weighted browser/home area
	 * is measured in the actually visible region. Older releases keep the
	 * manifest's adjustResize behavior.
	 */
	private static final class BrowserTab {
		String title; String url; GeckoSession.SessionState state;
		BrowserTab(String title, String url, GeckoSession.SessionState state) { this.title = title; this.url = url; this.state = state; }
	}

	private void applyImeInsetsWorkaround() {
		if (Build.VERSION.SDK_INT < 30) return;
		final View content = findViewById(android.R.id.content);
		if (content == null) return;
		final int initialLeft = content.getPaddingLeft();
		final int initialTop = content.getPaddingTop();
		final int initialRight = content.getPaddingRight();
		final int initialBottom = content.getPaddingBottom();
		content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
				android.graphics.Insets bars = insets.getInsets(
					WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
				android.graphics.Insets ime = insets.getInsets(WindowInsets.Type.ime());
				int bottomInset = Math.max(bars.bottom, ime.bottom);
				view.setPadding(initialLeft, Math.max(initialTop, bars.top), initialRight,
					Math.max(initialBottom, bottomInset));
				return insets;
			}
		});
		content.requestApplyInsets();
	}

	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		tela1 = findViewById(R.id.tela1);
		tela2 = findViewById(R.id.tela2);
		tela3 = findViewById(R.id.tela3);
		text_icon = findViewById(R.id.text_icon);
		linear_barra_pesquisa = findViewById(R.id.linear_barra_pesquisa);
		edittext1 = findViewById(R.id.edittext1);
		linear20 = findViewById(R.id.linear20);
		linear_b = findViewById(R.id.linear_b);
		vazio = findViewById(R.id.vazio);
		geckoView = findViewById(R.id.geckoview);
		browserContentContainer = findViewById(R.id.browser_content_container);

		text_barra = findViewById(R.id.text_barra);
		button_site_info = findViewById(R.id.button_site_info);
		button_menu = findViewById(R.id.button_menu);
		prog = findViewById(R.id.prog);
		text1 = findViewById(R.id.text1);
		text2 = findViewById(R.id.text2);
		vscroll1 = findViewById(R.id.vscroll1);
		linear33 = findViewById(R.id.linear33);
		linear_tela3_base = findViewById(R.id.linear_tela3_base);

		linear_escurecer = findViewById(R.id.linear_escurecer);
		linear_JavaScript = findViewById(R.id.linear_JavaScript);
		linear_limpar_dados2 = findViewById(R.id.linear_limpar_dados2);

		checkbox_brilho = findViewById(R.id.checkbox_brilho);
		checkbox_wallpaper = findViewById(R.id.checkbox_wallpaper);
		homeShortcuts = findViewById(R.id.home_shortcuts);
		checkbox_javascript2 = findViewById(R.id.checkbox_javascript2);
		text5 = findViewById(R.id.text5);
		Dados = getSharedPreferences("Dados", Activity.MODE_PRIVATE);
		browserLibraryStore = new BrowserLibraryStore(this);

		checkbox_wallpaper.setChecked(Dados.getBoolean("wallpaper_enabled", true));
		checkbox_wallpaper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean enabled) {
				Dados.edit().putBoolean("wallpaper_enabled", enabled).apply();
				if (enabled) {
					setupUnsplashWallpaper();
				} else {
                    findViewById(R.id.wallpaper_credit).setVisibility(View.GONE);
					tela1.setBackgroundColor(themeColor(android.R.attr.colorBackground, R.color.snav_background));
					text_icon.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
				}
			}
		});
		// setChecked() runs before the listener is registered, so explicitly
		// apply the persisted/default enabled state on the first Activity render.
		if (checkbox_wallpaper.isChecked()) setupUnsplashWallpaper();
        findViewById(R.id.button_tools_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { returnFromTools(); }
        });
        setupPageErrorView();
        refreshHomeShortcuts();


		linear_barra_pesquisa.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) { openSearchScreen(); }
		});
		edittext1.setFocusableInTouchMode(true);
		edittext1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override public void onFocusChange(View view, boolean hasFocus) {
				if (hasFocus) showSearchKeyboard();
			}
		});
		edittext1.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) { showSearchKeyboard(); }
		});

		ImageView searchAction = findViewById(R.id.image_search_action);
		if (searchAction != null) {
			searchAction.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					submitSearch();
				}
			});
		}
		
		linear_b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
		});

		if (button_menu != null) {
			button_menu.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View view) { showMenuBottomSheet(); }
			});
		}
        findViewById(R.id.button_extensions).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { if (extensionManager != null) extensionManager.show(); }
        });
        findViewById(R.id.button_home_menu).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { showMenuBottomSheet(); }
        });


		button_site_info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				showSiteInfoBottomSheet();
			}
		});
		
		text2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
		});
		

		
		linear_limpar_dados2.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				showSnavDecisionSheet("Limpar dados de navegação?",
					"O histórico e os dados de sites serão removidos. Favoritos e páginas fixadas serão mantidos.",
					"Limpar", "Cancelar", new Runnable() {
						@Override public void run() { clearGeckoBrowsingData(); }
					}, null);
			}
		});
		

		checkbox_brilho.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("brilho", "ativado").apply();
					geckoView.setAlpha(0.5f);
				} else {
					Dados.edit().putString("brilho", "desativado").apply();
					geckoView.setAlpha(1f);
				}
			}
		});
		
		checkbox_javascript2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("java", "ativado").apply();
					javaScriptEnabled = true;
					if (geckoEngine != null) geckoEngine.setJavaScriptEnabled(true); 
				} else {
					javaScriptEnabled = false;
					if (geckoEngine != null) geckoEngine.setJavaScriptEnabled(false); 
					Dados.edit().putString("java", "desativado").apply();
				}
			}
		});
		

	}
	

	private boolean submitSearch() {
		String input = edittext1 == null || edittext1.getText() == null
			? "" : edittext1.getText().toString().trim();
		if (input.isEmpty()) {
			openSearchScreen();
			return false;
		}

		String destination = resolveNavigationInput(input);
		if (destination == null) return false;

		if (destination.regionMatches(true, 0, "http://", 0, 7)) {
			showInsecureConnectionDialog(destination);
            return true;
		} else {
			loadUrlWithPrivacyHeaders(destination);
		}
        showScreen(2);
		vazio.setVisibility(View.GONE);

		edittext1.setText("");
		edittext1.clearFocus();
		android.view.inputmethod.InputMethodManager keyboard =
			(android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (keyboard != null) {
			keyboard.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
		}
		Dados.edit().putString("vazio", "desativado").apply();
		logDiagnostic("Pesquisa enviada pelo botão ou teclado");
		return true;
	}

	private void openSearchScreen() {
        showScreen(1);
		if (edittext1 == null) return;

		edittext1.setShowSoftInputOnFocus(true);
		getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		edittext1.requestFocus();
		showSearchKeyboard();
	}

	private void showSearchKeyboard() {
		if (edittext1 == null) return;
		edittext1.post(new Runnable() {
			@Override public void run() {
				if (isFinishing() || isDestroyed() || !edittext1.isShown() || !edittext1.hasFocus()) return;
				android.view.inputmethod.InputMethodManager keyboard =
					(android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (keyboard != null) keyboard.showSoftInput(edittext1,
					android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
			}
		});
	}

	private static final String DIAGNOSTIC_FILE = "snav-diagnostic.log";
	private static final String PREVIOUS_DIAGNOSTIC_FILE = "snav-diagnostic.log.old";
	private static final String PENDING_CRASH_FILE = "snav-crash-pending";
	private static final int MAX_DIAGNOSTIC_BYTES = 262144;
	private static volatile java.io.File diagnosticDirectory;
	private static boolean diagnosticCrashHandlerInstalled;

	private static final java.util.concurrent.ThreadPoolExecutor DIAGNOSTIC_EXECUTOR =
		new java.util.concurrent.ThreadPoolExecutor(
			1, 1, 0L, java.util.concurrent.TimeUnit.MILLISECONDS,
			new java.util.concurrent.ArrayBlockingQueue<Runnable>(128),
			new java.util.concurrent.ThreadFactory() {
				@Override
				public Thread newThread(Runnable task) {
					Thread thread = new Thread(task, "Snav-Diagnostics");
					thread.setDaemon(true);
					return thread;
				}
			},
			new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy()
		);

	private String resolveNavigationInput(String input) {
		String value = input == null ? "" : input.trim();
		if (value.isEmpty()) return null;
		if (value.matches("(?i)^https?://.*")) {
			if (value.matches(".*\\s+.*")) return buildSearchUrl(value);
			try {
				Uri parsed = Uri.parse(value);
				if (parsed.getHost() != null) return value;
			} catch (Exception ignored) {
			}
			return buildSearchUrl(value);
		}

		if (!value.matches(".*\\s+.*") && isDomainOrIp(value)) {
			String candidate = "https://" + value;
			try {
				if (Uri.parse(candidate).getHost() != null) return candidate;
			} catch (Exception ignored) {
			}
		}
		return buildSearchUrl(value);
	}

	private boolean isDomainOrIp(String value) {
		if (value == null || value.isEmpty()) return false;
		return value.matches("(?i)^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}(?::\\d{1,5})?(?:[/?#][^\\s]*)?$")
			|| value.matches("^(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?(?:[/?#][^\\s]*)?$")
			|| value.matches("(?i)^localhost(?::\\d{1,5})?(?:/[^\\s]*)?$");
	}

	private String buildSearchUrl(String query) {
		try {
			return "https://www.google.com/search?q="
				+ java.net.URLEncoder.encode(query, "UTF-8");
		} catch (Exception error) {
			logDiagnostic("Falha ao codificar consulta de pesquisa");
			return null;
		}
	}

	private void startDiagnostics() {
		final java.io.File directory = getFilesDir();
		diagnosticDirectory = directory;
		synchronized (MainActivity.class) {
			if (!diagnosticCrashHandlerInstalled) {
				final Thread.UncaughtExceptionHandler previous =
					Thread.getDefaultUncaughtExceptionHandler();
				try {
					Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(Thread thread, Throwable error) {
							writeDiagnosticSynchronously(directory,
								"CRASH NÃO TRATADO na thread " + thread.getName(), error);
							try {
								java.io.File marker = new java.io.File(directory, PENDING_CRASH_FILE);
								java.io.FileOutputStream output = new java.io.FileOutputStream(marker, false);
								output.write(String.valueOf(System.currentTimeMillis()).getBytes("UTF-8"));
								output.close();
							} catch (Throwable markerError) {
								android.util.Log.e("SnavDiagnostic", "Falha ao salvar marcador de crash", markerError);
							}
							if (previous != null) {
								previous.uncaughtException(thread, error);
							} else {
								android.os.Process.killProcess(android.os.Process.myPid());
								System.exit(10);
							}
						}
					});
					diagnosticCrashHandlerInstalled = true;
				} catch (Throwable handlerError) {
					android.util.Log.e("SnavDiagnostic", "Não foi possível instalar o capturador de falhas", handlerError);
				}
			}
		}
		logDiagnostic("========== INICIALIZAÇÃO DO SNAV ==========");
		logDiagnostic("Android=" + android.os.Build.VERSION.RELEASE
			+ " (SDK " + android.os.Build.VERSION.SDK_INT + ")");
		logDiagnostic("Dispositivo=" + android.os.Build.MANUFACTURER
			+ " " + android.os.Build.MODEL);
		logDiagnostic("Processo=" + android.os.Process.myPid());
	}

	private void logDiagnostic(final String event) {
		final java.io.File directory = diagnosticDirectory;
		if (directory == null || event == null) return;
		try {
			DIAGNOSTIC_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					writeDiagnosticSynchronously(directory, event, null);
				}
			});
		} catch (RuntimeException ignored) {
			android.util.Log.w("SnavDiagnostic", "Evento de diagnóstico descartado");
		}
	}

	private static synchronized void writeDiagnosticSynchronously(
		java.io.File directory, String event, Throwable error) {
		if (directory == null) return;
		java.io.FileOutputStream output = null;
		java.io.OutputStreamWriter writer = null;
		try {
			if (!directory.exists() && !directory.mkdirs()) return;
			java.io.File file = new java.io.File(directory, DIAGNOSTIC_FILE);
			if (file.exists() && file.length() >= MAX_DIAGNOSTIC_BYTES) {
				java.io.File previous = new java.io.File(directory, PREVIOUS_DIAGNOSTIC_FILE);
				if (previous.exists()) previous.delete();
				if (!file.renameTo(previous)) file.delete();
			}
			output = new java.io.FileOutputStream(file, true);
			writer = new java.io.OutputStreamWriter(output, "UTF-8");
			writer.write(new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date()));
			writer.write(" [");
			writer.write(Thread.currentThread().getName());
			writer.write("] ");
			writer.write(event);
			writer.write("\n");
			if (error != null) {
				java.io.StringWriter trace = new java.io.StringWriter();
				error.printStackTrace(new java.io.PrintWriter(trace));
				String stack = trace.toString();
				if (stack.length() > 32768) stack = stack.substring(0, 32768);
				writer.write(stack);
				writer.write("\n");
			}
			writer.flush();
		} catch (Throwable logError) {
			android.util.Log.e("SnavDiagnostic", "Falha ao gravar diagnóstico", logError);
		} finally {
			try {
				if (writer != null) writer.close();
				else if (output != null) output.close();
			} catch (Exception ignored) {
			}
		}
	}

	private String safeHostForDiagnostics(String address) {
		try {
			String host = address == null ? null : Uri.parse(address).getHost();
			return host == null ? "(host indisponível)" : host;
		} catch (Exception ignored) {
			return "(host inválido)";
		}
	}

	private void addDiagnosticMenuAction() {
		if (linear33 == null) return;
		TextView action = new TextView(this);
		action.setText("Copiar diagnóstico");
		action.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		action.setTextSize(14);
		action.setGravity(Gravity.CENTER_VERTICAL);
		int horizontal = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
		int vertical = (int) (14 * getResources().getDisplayMetrics().density + 0.5f);
		action.setPadding(horizontal, vertical, horizontal, vertical);
		android.util.TypedValue rippleBackground = new android.util.TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleBackground, true)) {
			action.setBackgroundResource(rippleBackground.resourceId);
		}
		action.setClickable(true);
		action.setFocusable(true);
		action.setContentDescription("Copiar relatório de diagnóstico do Snav");
		action.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				copyDiagnosticReport();
			}
		});
		linear33.addView(action, new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	private void showPendingCrashReport() {
		final java.io.File marker = new java.io.File(getFilesDir(), PENDING_CRASH_FILE);
		if (!marker.exists()) return;
		showSnavDecisionSheet("O Snav foi fechado",
			"O Snav foi encerrado inesperadamente. Copie o diagnóstico para ajudar a identificar a causa.",
			"Copiar diagnóstico", "Depois", new Runnable() {
				@Override public void run() { if (copyDiagnosticReport()) marker.delete(); }
			}, null);
	}

	private boolean copyDiagnosticReport() {
		try {
			StringBuilder report = new StringBuilder();
			report.append("SNAV BROWSER - RELATÓRIO DE DIAGNÓSTICO\n");
			report.append("Android: ").append(android.os.Build.VERSION.RELEASE)
				.append(" (SDK ").append(android.os.Build.VERSION.SDK_INT).append(")\n");
			report.append("Dispositivo: ").append(android.os.Build.MANUFACTURER)
				.append(" ").append(android.os.Build.MODEL).append("\n\n");
			appendDiagnosticFile(report, new java.io.File(getFilesDir(), PREVIOUS_DIAGNOSTIC_FILE));
			appendDiagnosticFile(report, new java.io.File(getFilesDir(), DIAGNOSTIC_FILE));
			ClipboardManager clipboard =
				(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			if (clipboard == null) return false;
			clipboard.setPrimaryClip(ClipData.newPlainText("Snav diagnóstico", report.toString()));
			Toast.makeText(this, "Diagnóstico copiado. Revise o conteúdo antes de compartilhar.",
				Toast.LENGTH_LONG).show();
			return true;
		} catch (Exception error) {
			android.util.Log.e("SnavDiagnostic", "Falha ao copiar diagnóstico", error);
			Toast.makeText(this, "Não foi possível copiar o diagnóstico", Toast.LENGTH_LONG).show();
			return false;
		}
	}

	private void appendDiagnosticFile(StringBuilder report, java.io.File file) throws IOException {
		if (!file.exists()) return;
		report.append("--- ").append(file.getName()).append(" ---\n");
		java.io.BufferedReader reader = new java.io.BufferedReader(
			new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"));
		try {
			String line;
			int total = 0;
			while ((line = reader.readLine()) != null && total < MAX_DIAGNOSTIC_BYTES * 2) {
				report.append(line).append('\n');
				total += line.length();
			}
		} finally {
			reader.close();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
        if (tela1 != null && tela2 != null && tela3 != null) {
            outState.putInt("snav_visible_screen", tela3.getVisibility() == View.VISIBLE ? 3 : tela2.getVisibility() == View.VISIBLE ? 2 : 1);
            outState.putBoolean("snav_tools_from_browser", toolsOpenedFromBrowser);
        }
		if (geckoEngine != null) {
			geckoEngine.flushSessionState();
			GeckoSession.SessionState state = geckoEngine.getSessionState();
			if (state != null) outState.putParcelable(STATE_GECKO_SESSION, state);
			String uri = geckoEngine.getCurrentUri();
			if (uri != null) outState.putString(STATE_GECKO_URI, uri);
			if (!browserTabs.isEmpty()) {
				BrowserTab active = browserTabs.get(activeBrowserTab);
				active.state = state == null ? geckoEngine.getSessionState() : new GeckoSession.SessionState(state);
				active.url = uri; active.title = currentPageTitle;
				ArrayList<Bundle> tabBundles = new ArrayList<>();
				for (BrowserTab tab : browserTabs) { Bundle item = new Bundle(); item.putString("title", tab.title); item.putString("url", tab.url); if (tab.state != null) item.putParcelable("state", tab.state); tabBundles.add(item); }
				outState.putParcelableArrayList(STATE_BROWSER_TABS, tabBundles); outState.putInt(STATE_ACTIVE_TAB, activeBrowserTab);
			}
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (geckoEngine != null) geckoEngine.setActive(true);
        if (extensionManager != null) {
            extensionManager.setActive(true);
            if (pendingExtensionFile != null) {
                Uri file = pendingExtensionFile;
                pendingExtensionFile = null;
                extensionManager.installFile(file);
            }
        }
		logDiagnostic("Ciclo de vida: onResume");
	
		clearGrantedPermissionDenials();}

	@Override
	protected void onPause() {
        if (extensionManager != null) extensionManager.setActive(false);
		if (geckoEngine != null) geckoEngine.setActive(false);
		logDiagnostic("Ciclo de vida: onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logDiagnostic("Ciclo de vida: onDestroy; isFinishing=" + isFinishing());
		if (activePanel != null) activePanel.dismiss();
		wallpaperCallback = null;
		readerRequestId++;
		readerModePending = false;
		if (pendingGeckoDownload != null) {
			closeResponseBody(pendingGeckoDownload);
			pendingGeckoDownload = null;
		}
		if (pendingAndroidPermissionCallback != null) {
			pendingAndroidPermissionCallback.reject();
			pendingAndroidPermissionCallback = null;
		}
		if (pendingMediaPermissionCallback != null) {
			pendingMediaPermissionCallback.reject();
			pendingMediaPermissionCallback = null;
		}
		pendingAndroidPermissions = null;
        for (GeckoResult<Integer> pending : new java.util.ArrayList<>(pendingContentPermissions)) {
            finishContentPermission(pending, false);
        }
		if (downloadExecutor != null) downloadExecutor.shutdown();
		if (translationThread != null) translationThread.quitSafely();
        faviconExecutor.shutdownNow();
        if (extensionManager != null) { extensionManager.close(); extensionManager = null; }
		GeckoEngineController engineToClose = geckoEngine;
		geckoEngine = null;
		if (engineToClose != null) engineToClose.close();
		super.onDestroy();
	}

	private final Handler progressHandler = new Handler(Looper.getMainLooper());
	private ObjectAnimator progressAnimator;
	private Runnable showProgressRunnable;
	private boolean pageLoading = false;

	private void beginPageProgress() {
		pageLoading = true;
		if (showProgressRunnable != null) {
			progressHandler.removeCallbacks(showProgressRunnable);
		}
		if (progressAnimator != null) {
			progressAnimator.cancel();
		}
		prog.animate().cancel();
		prog.setAlpha(1f);
		prog.setProgress(0);
		prog.setIndeterminate(true);

		showProgressRunnable = new Runnable() {
			@Override
			public void run() {
				if (pageLoading) {
					prog.setVisibility(View.VISIBLE);
				}
			}
		};
		progressHandler.postDelayed(showProgressRunnable, 120);
	}

	private void finishPageProgress() {
		if (!pageLoading) {
			return;
		}
		pageLoading = false;
        if (refreshHost != null) refreshHost.setRefreshing(false);
		if (showProgressRunnable != null) {
			progressHandler.removeCallbacks(showProgressRunnable);
		}
		if (progressAnimator != null) {
			progressAnimator.cancel();
		}
		prog.setIndeterminate(false);
		prog.setProgress(100);

		if (prog.getVisibility() != View.VISIBLE) {
			prog.setProgress(0);
			return;
		}

		prog.animate()
			.alpha(0f)
			.setDuration(160)
			.withEndAction(new Runnable() {
				@Override
				public void run() {
					prog.setVisibility(View.GONE);
					prog.setAlpha(1f);
					prog.setProgress(0);
				}
			})
			.start();
	}

	private void ensureTranslationThread() {
		if (translationThread != null && translationThread.isAlive()) return;
		translationThread = new HandlerThread("SnavTranslations");
		translationThread.start();
		translationHandler = new Handler(translationThread.getLooper());
	}

	private int findLanguageIndex(List<TranslationsController.Language> languages, String code) {
		if (languages == null || code == null) return 0;
		for (int i = 0; i < languages.size(); i++) {
			if (code.equalsIgnoreCase(languages.get(i).code)) return i;
		}
		return 0;
	}

	private void showTranslationBottomSheet() {
		if (geckoEngine == null || !isSecureOrigin(geckoEngine.getCurrentUri())) {
			Toast.makeText(this, "Abra uma página HTTPS para traduzir", Toast.LENGTH_SHORT).show();
			return;
		}
		final BottomSheetDialog sheet = new BottomSheetDialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(dp(24), dp(8), dp(24), dp(28));

		BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
		content.addView(handle, new LinearLayout.LayoutParams(-1, dp(48)));
		TextView title = new TextView(this);
		title.setText("Traduzir página");
		title.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		LinearLayout.LayoutParams translationTitleParams = new LinearLayout.LayoutParams(-1, -2);
		translationTitleParams.bottomMargin = dp(8);
		content.addView(title, translationTitleParams);
		TextView subtitle = new TextView(this);
		subtitle.setText("Os idiomas necessários podem ser baixados na primeira tradução");
		subtitle.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		subtitle.setTextSize(13);
		subtitle.setPadding(0, dp(6), 0, dp(16));
		content.addView(subtitle);
		final TextInputLayout fromLayout = new TextInputLayout(this);
		fromLayout.setHint("Idioma da página");
		fromLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
		final MaterialAutoCompleteTextView fromInput = new MaterialAutoCompleteTextView(this);
		fromInput.setInputType(android.text.InputType.TYPE_NULL);
		fromInput.setKeyListener(null);
		fromInput.setThreshold(0);
		fromLayout.addView(fromInput, new LinearLayout.LayoutParams(-1, -2));
		LinearLayout.LayoutParams fromParams = new LinearLayout.LayoutParams(-1, -2);
		fromParams.topMargin = dp(4);
		content.addView(fromLayout, fromParams);
		final TextInputLayout toLayout = new TextInputLayout(this);
		toLayout.setHint("Traduzir para");
		toLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
		final MaterialAutoCompleteTextView toInput = new MaterialAutoCompleteTextView(this);
		toInput.setInputType(android.text.InputType.TYPE_NULL);
		toInput.setKeyListener(null);
		toInput.setThreshold(0);
		toLayout.addView(toInput, new LinearLayout.LayoutParams(-1, -2));
		LinearLayout.LayoutParams toParams = new LinearLayout.LayoutParams(-1, -2);
		toParams.topMargin = dp(12);
		content.addView(toLayout, toParams);
		final TextView status = new TextView(this);
		status.setText("Carregando idiomas…");
		status.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		status.setTextSize(12);
		status.setPadding(0, dp(8), 0, dp(12));
		content.addView(status);
		LinearLayout actions = new LinearLayout(this);
		actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
		MaterialButton restore = new MaterialButton(this);
		restore.setText("Ver original");
		restore.setAllCaps(false);
		restore.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		restore.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface)));
		restore.setStrokeColor(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorOutlineVariant, R.color.snav_outline)));
		restore.setStrokeWidth(dp(1));
		restore.setCornerRadius(dp(20));
		LinearLayout.LayoutParams restoreParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
		actions.addView(restore, restoreParams);
		MaterialButton translate = new MaterialButton(this);
		translate.setText("Traduzir");
		translate.setAllCaps(false);
		translate.setCornerRadius(dp(20));
		translate.setEnabled(false);
		LinearLayout.LayoutParams translateParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
		translateParams.leftMargin = dp(12);
		actions.addView(translate, translateParams);
		LinearLayout.LayoutParams translationActionsParams = new LinearLayout.LayoutParams(-1, -2);
		translationActionsParams.topMargin = dp(8);
		content.addView(actions, translationActionsParams);
		ScrollView scroll = new ScrollView(this);
		scroll.setFillViewport(true);
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
		sheet.setContentView(scroll);
		sheet.show();
		sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
		sheet.getBehavior().setSkipCollapsed(true);

		final ArrayList<TranslationsController.Language> sourceLanguages = new ArrayList<>();
		final ArrayList<TranslationsController.Language> targetLanguages = new ArrayList<>();
		final ArrayList<String> sourceNames = new ArrayList<>();
		final ArrayList<String> targetNames = new ArrayList<>();
		ensureTranslationThread();
		translationHandler.post(new Runnable() {
			@Override public void run() {
				TranslationsController.RuntimeTranslation.listSupportedLanguages().accept(
					new GeckoResult.Consumer<TranslationsController.RuntimeTranslation.TranslationSupport>() {
						@Override public void accept(final TranslationsController.RuntimeTranslation.TranslationSupport support) {
							runOnUiThread(new Runnable() {
								@Override public void run() {
									if (isFinishing() || isDestroyed() || !sheet.isShowing()) return;
									if (support == null || support.fromLanguages == null || support.toLanguages == null
										|| support.fromLanguages.isEmpty() || support.toLanguages.isEmpty()) {
										status.setText("A tradução não está disponível neste dispositivo");
										return;
									}
									sourceLanguages.clear();
									targetLanguages.clear();
									sourceLanguages.addAll(support.fromLanguages);
									targetLanguages.addAll(support.toLanguages);
									Collections.sort(sourceLanguages);
									Collections.sort(targetLanguages);
									sourceNames.clear();
									targetNames.clear();
									for (TranslationsController.Language language : sourceLanguages) sourceNames.add(language.localizedDisplayName == null ? language.code : language.localizedDisplayName);
									for (TranslationsController.Language language : targetLanguages) targetNames.add(language.localizedDisplayName == null ? language.code : language.localizedDisplayName);
									ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, sourceNames);
									fromInput.setAdapter(sourceAdapter);
									ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, targetNames);
									toInput.setAdapter(targetAdapter);
									fromInput.setText(sourceNames.get(findLanguageIndex(sourceLanguages, detectedPageLanguage)), false);
									toInput.setText(targetNames.get(findLanguageIndex(targetLanguages, "pt")), false);
									status.setText(detectedPageLanguage == null
										? "Selecione o idioma da página"
										: "Idioma detectado: " + detectedPageLanguage);
									translate.setEnabled(true);
								}
							});
						}
					},
					new GeckoResult.Consumer<Throwable>() {
						@Override public void accept(final Throwable error) {
							runOnUiThread(new Runnable() {
								@Override public void run() {
									if (!isFinishing() && !isDestroyed() && sheet.isShowing()) status.setText("Não foi possível carregar os idiomas. Tente novamente.");
								}
							});
						}
					});
			}
		});
		translate.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (sourceLanguages.isEmpty() || targetLanguages.isEmpty() || geckoEngine == null) return;
				int fromIndex = sourceNames.indexOf(fromInput.getText().toString());
				int toIndex = targetNames.indexOf(toInput.getText().toString());
				if (fromIndex < 0 || toIndex < 0) {
					status.setText("Selecione os idiomas da página e da tradução");
					return;
				}
				final String from = sourceLanguages.get(fromIndex).code;
				final String to = targetLanguages.get(toIndex).code;
				if (from.equalsIgnoreCase(to)) {
					status.setText("Escolha idiomas diferentes");
					return;
				}
				status.setText("Preparando tradução…");
				translate.setEnabled(false);
				translationHandler.post(new Runnable() {
					@Override public void run() {
						if (isFinishing() || isDestroyed() || geckoEngine == null) return;
						geckoEngine.getSession().getSessionTranslation().translate(from, to,
							new TranslationsController.SessionTranslation.TranslationOptions.Builder().downloadModel(true).build()
						).accept(
							new GeckoResult.Consumer<Void>() {
								@Override public void accept(Void ignored) {
									runOnUiThread(new Runnable() {
										@Override public void run() {
											if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Tradução iniciada", Toast.LENGTH_SHORT).show();
											sheet.dismiss();
										}
									});
								}
							},
							new GeckoResult.Consumer<Throwable>() {
								@Override public void accept(final Throwable error) {
									runOnUiThread(new Runnable() {
										@Override public void run() {
											translate.setEnabled(true);
											status.setText("Não foi possível iniciar a tradução. Os idiomas selecionados podem não estar disponíveis.");
											logDiagnostic("Falha na tradução Gecko: " + error.getClass().getSimpleName());
										}
									});
								}
							}
						);
					}
				});
			}
		});
		restore.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (geckoEngine == null) return;
				translationHandler.post(new Runnable() {
					@Override public void run() {
						if (isFinishing() || isDestroyed() || geckoEngine == null) return;
						geckoEngine.getSession().getSessionTranslation().restoreOriginalPage().accept(
							new GeckoResult.Consumer<Void>() {
								@Override public void accept(Void ignored) {
									runOnUiThread(new Runnable() {
										@Override public void run() { sheet.dismiss(); }
									});
								}
							},
							new GeckoResult.Consumer<Throwable>() {
								@Override public void accept(Throwable error) {
									runOnUiThread(new Runnable() {
										@Override public void run() { status.setText("A página já está no idioma original ou não foi possível restaurá-la"); }
									});
								}
							}
						);
					}
				});
			}
		});
	}

	private void showReaderOptionsBottomSheet() {
		final String current = geckoEngine == null ? null : geckoEngine.getCurrentUri();
		if (current == null || !isSecureOrigin(current)) {
			Toast.makeText(this, "Abra uma página HTTPS para usar o modo de leitura", Toast.LENGTH_SHORT).show();
			return;
		}
		final BottomSheetDialog sheet = new BottomSheetDialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(dp(24), dp(8), dp(24), dp(28));

		BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
		content.addView(handle, new LinearLayout.LayoutParams(-1, dp(48)));
		TextView title = new TextView(this);
		title.setText("Modo de leitura");
		title.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		content.addView(title);
		TextView description = new TextView(this);
		description.setText("Mostra o conteúdo principal da página em uma visualização simplificada");
		description.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		description.setTextSize(14);
		description.setPadding(0, dp(8), 0, dp(20));
		content.addView(description);
		MaterialButton action = new MaterialButton(this);
		action.setText(readerMode ? "Voltar à página original" : "Ativar modo de leitura");
		action.setAllCaps(false);
		action.setCornerRadius(dp(20));
		LinearLayout.LayoutParams readerActionParams = new LinearLayout.LayoutParams(-1, dp(48));
		readerActionParams.topMargin = dp(8);
		content.addView(action, readerActionParams);
		action.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (readerMode) {
					exitReaderMode();
				} else if (!readerModePending) {
					readerOriginalUri = current;
					readerModePending = true;
					activateReaderMode(current);
				}
				sheet.dismiss();
			}
		});
		ScrollView scroll = new ScrollView(this);
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
		sheet.setContentView(scroll);
		sheet.show();
		sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
		sheet.getBehavior().setSkipCollapsed(true);
	}

	private void activateReaderMode(final String sourceUri) {
		if (geckoEngine == null || !isSecureOrigin(sourceUri)) {
			readerModePending = false;
			readerOriginalUri = null;
			Toast.makeText(this, "Não foi possível preparar a página para leitura", Toast.LENGTH_LONG).show();
			return;
		}
		final GeckoSession sourceSession = geckoEngine.getSession();
		final int requestId = ++readerRequestId;
		ensureTranslationThread();
		translationHandler.post(new Runnable() {
			@Override public void run() {
				try {
					sourceSession.getSessionPageExtractor().getPageMetadata().accept(
						new GeckoResult.Consumer<PageExtractionController.PageMetadata>() {
							@Override public void accept(final PageExtractionController.PageMetadata metadata) {
								if (metadata == null || !metadata.isReaderable) {
								showReaderExtractionError(requestId, sourceUri, sourceSession,
									"Esta página não possui um artigo compatível com o modo de leitura");
									return;
								}
								translationHandler.post(new Runnable() {
									@Override public void run() {
										try {
											sourceSession.getSessionPageExtractor().getPageContent(
												new PageExtractionController.ContentParams(true, true)
											).accept(
											new GeckoResult.Consumer<String>() {
												@Override public void accept(final String article) {
													runOnUiThread(new Runnable() {
														@Override public void run() {
															if (!isCurrentReaderRequest(requestId, sourceUri, sourceSession)) return;
															if (article == null || article.trim().isEmpty()) {
																showReaderExtractionErrorOnUi("Não foi possível extrair o conteúdo principal desta página");
																return;
															}
															displayReaderContent(sourceUri, article);
														}
													});
												}
											},
											new GeckoResult.Consumer<Throwable>() {
												@Override public void accept(Throwable error) {
													runOnUiThread(new Runnable() {
													@Override public void run() {
														if (isCurrentReaderRequest(requestId, sourceUri, sourceSession)) {
															showReaderExtractionErrorOnUi("Não foi possível extrair o conteúdo desta página");
														}
													}
												});
												}
											}
										);
										} catch (RuntimeException error) {
										showReaderExtractionError(requestId, sourceUri, sourceSession,
											"Não foi possível extrair o conteúdo desta página");
									}
								}
							});
							}
						},
						new GeckoResult.Consumer<Throwable>() {
							@Override public void accept(Throwable error) {
								showReaderExtractionError(requestId, sourceUri, sourceSession,
									"Não foi possível analisar esta página para leitura");
							}
						}
					);
				} catch (RuntimeException error) {
					showReaderExtractionError(requestId, sourceUri, sourceSession,
						"Não foi possível iniciar o modo de leitura");
				}
			}
		});
	}

	private boolean isCurrentReaderRequest(int requestId, String sourceUri, GeckoSession sourceSession) {
		return !isFinishing() && !isDestroyed() && readerModePending
			&& readerRequestId == requestId && geckoEngine != null
			&& geckoEngine.getSession() == sourceSession
			&& sourceUri.equals(geckoEngine.getCurrentUri());
	}

	private void showReaderExtractionError(int requestId, String sourceUri,
		GeckoSession sourceSession, final String message) {
		runOnUiThread(new Runnable() {
			@Override public void run() {
				if (isCurrentReaderRequest(requestId, sourceUri, sourceSession)) {
					showReaderExtractionErrorOnUi(message);
				}
			}
		});
	}

	private void showReaderExtractionErrorOnUi(String message) {
		readerModePending = false;
		readerOriginalUri = null;
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void displayReaderContent(String sourceUri, String article) {
		if (browserContentContainer == null || geckoView == null) {
			showReaderExtractionErrorOnUi("A visualização de leitura não está disponível");
			return;
		}
		if (readerContentView != null) browserContentContainer.removeView(readerContentView);
		LinearLayout page = new LinearLayout(this);
		page.setOrientation(LinearLayout.VERTICAL);
		page.setPadding(dp(24), dp(24), dp(24), dp(32));
		page.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface));

		TextView eyebrow = new TextView(this);
		eyebrow.setText("Modo de leitura");
		eyebrow.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		eyebrow.setTextSize(12);
		eyebrow.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		page.addView(eyebrow);

		String heading = currentPageTitle == null || currentPageTitle.trim().isEmpty()
			? Uri.parse(sourceUri).getHost() : currentPageTitle.trim();
		TextView title = new TextView(this);
		title.setText(heading == null ? "Artigo" : heading);
		title.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		title.setTextSize(25);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setLineSpacing(dp(2), 1.05f);
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
		titleParams.topMargin = dp(10);
		page.addView(title, titleParams);

		TextView source = new TextView(this);
		source.setText(Uri.parse(sourceUri).getHost() == null ? sourceUri : Uri.parse(sourceUri).getHost());
		source.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		source.setTextSize(13);
		LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(-1, -2);
		sourceParams.topMargin = dp(8);
		sourceParams.bottomMargin = dp(20);
		page.addView(source, sourceParams);

		MaterialButton back = new MaterialButton(this);
		back.setText("Voltar à página original");
		back.setAllCaps(false);
		back.setCornerRadius(dp(20));
		page.addView(back, new LinearLayout.LayoutParams(-1, dp(48)));
		back.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) { exitReaderMode(); }
		});

		TextView body = new TextView(this);
		body.setText(article);
		body.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		body.setTextSize(18);
		body.setLineSpacing(dp(8), 1.18f);
		body.setTextIsSelectable(true);
		LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, -2);
		bodyParams.topMargin = dp(24);
		page.addView(body, bodyParams);

		ScrollView readerScroll = new ScrollView(this);
		readerScroll.setFillViewport(false);
		readerScroll.setClipToPadding(false);
		readerScroll.addView(page, new ScrollView.LayoutParams(-1, -2));
		readerContentView = readerScroll;
		browserContentContainer.addView(readerContentView,
			new FrameLayout.LayoutParams(-1, -1));
		geckoView.setVisibility(View.GONE);
		readerModePending = false;
		readerMode = true;
	}

	private void exitReaderMode() {
		if (readerContentView != null && browserContentContainer != null) {
			browserContentContainer.removeView(readerContentView);
		}
		readerContentView = null;
		if (geckoView != null) geckoView.setVisibility(View.VISIBLE);
		readerMode = false;
		readerModePending = false;
		readerOriginalUri = null;
		readerRequestId++;
	}

	private int siteInfoDp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void addSiteInfoRow(LinearLayout parent, String title, String value) {
		TextView heading = new TextView(this);
		heading.setText(title);
		heading.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		heading.setTextSize(12);
		parent.addView(heading);

		TextView detail = new TextView(this);
		detail.setText(value);
		detail.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		detail.setTextSize(14);
		detail.setTextIsSelectable(true);
		detail.setPadding(0, siteInfoDp(3), 0, siteInfoDp(12));
		parent.addView(detail);
	}


    private void showScreen(int screen) {
        tela1.setVisibility(screen == 1 ? View.VISIBLE : View.GONE);
        tela2.setVisibility(screen == 2 ? View.VISIBLE : View.GONE);
        tela3.setVisibility(screen == 3 ? View.VISIBLE : View.GONE);
        linear_b.setVisibility(screen == 2 ? View.VISIBLE : View.GONE);
        updateTopBar();
        if (screen != 1 && edittext1 != null) {
            edittext1.clearFocus();
            android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
        }
        if (screen == 1) refreshHomeShortcuts();
    }

    private boolean hasUsablePage() {
        return geckoEngine != null && isSecureOrigin(geckoEngine.getCurrentUri())
            && !pageLoading && vazio.getVisibility() != View.VISIBLE
            && (pageErrorView == null || pageErrorView.getVisibility() != View.VISIBLE);
    }

    private boolean hasPageForActions() {
        return tela2.getVisibility() == View.VISIBLE && hasUsablePage();
    }

    private void updateTopBar() {
        if (linear_b == null || tela1 == null || text_barra == null || button_site_info == null) return;
        boolean browser = tela2.getVisibility() == View.VISIBLE;
        linear_b.setClickable(browser);
        text_barra.setVisibility(browser ? View.VISIBLE : View.GONE);
        button_site_info.setVisibility(browser && hasUsablePage() ? View.VISIBLE : View.GONE);
    }

    private void returnFromTools() {
        showScreen(toolsOpenedFromBrowser ? 2 : 1);
    }

    private void setupPageErrorView() {
        LinearLayout error = new LinearLayout(this);
        error.setOrientation(LinearLayout.VERTICAL);
        error.setGravity(Gravity.CENTER);
        error.setPadding(dp(24), dp(24), dp(24), dp(24));
        error.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface));
        TextView message = new TextView(this);
        message.setText("Não foi possível carregar a página. Verifique a conexão e tente novamente.");
        message.setGravity(Gravity.CENTER);
        message.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        message.setTextSize(16);
        error.addView(message);
        MaterialButton retry = new MaterialButton(this);
        retry.setText("Tentar novamente");
        retry.setAllCaps(false);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                pageErrorView.setVisibility(View.GONE);
                if (failedPageUrl != null) loadUrlWithPrivacyHeaders(failedPageUrl);
            }
        });
        error.addView(retry);
        error.setVisibility(View.GONE);
        browserContentContainer.addView(error, new FrameLayout.LayoutParams(-1, -1));
        pageErrorView = error;
    }

    private void refreshHomeShortcuts() {
        if (homeShortcuts == null || browserLibraryStore == null) return;
        homeShortcuts.removeAllViews();
        boolean visible = Dados.getBoolean("home_shortcuts_visible", true);
        ((View) homeShortcuts.getParent()).setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) return;
        List<BrowserLibraryStore.Entry> pinned = browserLibraryStore.getPinned();
        List<BrowserLibraryStore.Entry> recent = browserLibraryStore.getHistory();
        if (Dados.getBoolean("home_show_pinned", true) && !pinned.isEmpty()) addShortcutSection("Fixados", pinned, true);
        else if (Dados.getBoolean("home_show_pinned", true)) {
            TextView hint = new TextView(this);
            hint.setText("Para fixar até 4 páginas, use o menu ou mantenha um recente pressionado");
            hint.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
            hint.setTextSize(12);
            hint.setGravity(Gravity.CENTER);
            homeShortcuts.addView(hint);
        }
        if (Dados.getBoolean("home_show_recent", true) && !recent.isEmpty()) addShortcutSection("Recentes", recent, false);
        ((View) homeShortcuts.getParent()).setVisibility(homeShortcuts.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private void addShortcutSection(String title, List<BrowserLibraryStore.Entry> entries, final boolean pinned) {
        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextSize(14);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        homeShortcuts.addView(heading);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        homeShortcuts.addView(row, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 0; i < 4; i++) {
            if (i >= entries.size()) {
                row.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
                continue;
            }
            final BrowserLibraryStore.Entry entry = entries.get(i);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            item.setPadding(dp(2), dp(4), dp(2), dp(4));
            String name = entry.title == null || entry.title.trim().isEmpty() || entry.title.equals(entry.url)
                ? domainForAddressBar(entry.url) : entry.title;
            // Separate the circular MD3 button from the centered site image.
            FrameLayout iconContainer = new FrameLayout(this);
            MaterialButton icon = new MaterialButton(this);
            icon.setInsetTop(0);
            icon.setInsetBottom(0);
            icon.setMinWidth(0);
            icon.setMinimumWidth(0);
            icon.setPadding(0, 0, 0, 0);
            icon.setCornerRadius(dp(28));
            icon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorPrimaryContainer, R.color.snav_switch_primary_container)));
            icon.setContentDescription("Abrir " + name + ". Toque e segure para ver opções");
            iconContainer.addView(icon, new FrameLayout.LayoutParams(-1, -1));
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            image.setImageResource(R.drawable.ms_language);
            image.setImageTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface)));
            iconContainer.addView(image, new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.CENTER));
            item.addView(iconContainer, new LinearLayout.LayoutParams(dp(56), dp(56)));
            bindFavicon(image, entry.url);
            TextView label = new TextView(this);
            label.setText(name);
            label.setTextSize(12);
            label.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(1);
            label.setEllipsize(TextUtils.TruncateAt.END);
            item.addView(label, new LinearLayout.LayoutParams(-1, dp(22)));
            View.OnClickListener open = new View.OnClickListener() {
                @Override public void onClick(View view) {
                    if (readerMode) exitReaderMode();
                    loadUrlWithPrivacyHeaders(entry.url);
                    vazio.setVisibility(View.GONE);
                    showScreen(2);
                    edittext1.clearFocus();
                    android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (keyboard != null) keyboard.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
                }
            };
            View.OnLongClickListener manage = new View.OnLongClickListener() {
                @Override public boolean onLongClick(View view) {
                    showShortcutMenu(entry, pinned);
                    return true;
                }
            };
            icon.setOnClickListener(open);
            icon.setOnLongClickListener(manage);
            item.setOnClickListener(open);
            item.setOnLongClickListener(manage);
            row.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        }
    }

    private void showShortcutMenu(final BrowserLibraryStore.Entry entry, final boolean pinned) {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(8), dp(22), dp(24));
        root.addView(new BottomSheetDragHandleView(this), new LinearLayout.LayoutParams(-1, dp(42)));
        addMenuSheetRow(root, R.drawable.ms_add_box, "Adicionar site", true, new Runnable() {
            @Override public void run() { sheet.dismiss(); showShortcutEditor(null); }
        });
        if (entry != null) {
            addMenuSheetRow(root, R.drawable.ms_tab_duplicate, "Abrir em nova aba", true, new Runnable() {
                @Override public void run() { sheet.dismiss(); createBrowserTab(entry.url); }
            });
            addMenuSheetRow(root, R.drawable.ms_tab_unselected, "Abrir em aba privada", false, new Runnable() {
                @Override public void run() { }
            });
            addMenuSheetRow(root, R.drawable.ms_download, "Baixar link", true, new Runnable() {
                @Override public void run() { sheet.dismiss(); downloadShortcut(entry.url); }
            });
            if (pinned) {
                addMenuSheetRow(root, R.drawable.ms_edit_square, "Editar página fixada", true, new Runnable() {
                    @Override public void run() { sheet.dismiss(); showShortcutEditor(entry); }
                });
                addMenuSheetRow(root, R.drawable.ms_keep_off, "Desafixar página", true, new Runnable() {
                    @Override public void run() {
                        sheet.dismiss();
                        if (browserLibraryStore.unpin(entry.url)) {
                            refreshHomeShortcuts();
                            showFeedback("Página desafixada", "Desfazer", () -> {
                                boolean restored = browserLibraryStore.pin(entry.title, entry.url);
                                refreshHomeShortcuts();
                                if (!restored) showFeedback("Não foi possível restaurar a página fixada", null, null);
                            });
                        }
                    }
                });
            } else {
                addMenuSheetRow(root, R.drawable.ms_keep, "Fixar página", true, new Runnable() {
                    @Override public void run() {
                        sheet.dismiss();
                        boolean saved = browserLibraryStore.pin(entry.title, entry.url);
                        showFeedback(saved ? "Página fixada" : "Fixe até 4 páginas", null, null);
                        refreshHomeShortcuts();
                    }
                });
                addMenuSheetRow(root, R.drawable.ms_history, "Remover dos recentes", true, new Runnable() {
                    @Override public void run() {
                        sheet.dismiss(); browserLibraryStore.removeHistory(entry.url); refreshHomeShortcuts();
                        Toast.makeText(MainActivity.this, "Página removida dos recentes", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        View divider = new View(this);
        divider.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorOutlineVariant, R.color.snav_outline));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dp(1));
        dividerParams.topMargin = dp(8); dividerParams.bottomMargin = dp(8);
        root.addView(divider, dividerParams);
        addShortcutVisibilitySwitch(root, "Mostrar recentes", "home_show_recent");
        addShortcutVisibilitySwitch(root, "Mostrar fixados", "home_show_pinned");
        final boolean visible = Dados.getBoolean("home_shortcuts_visible", true);
        addMenuSheetRow(root, visible ? R.drawable.ms_visibility_off : R.drawable.ms_visibility,
            visible ? "Ocultar atalhos" : "Mostrar atalhos", true, new Runnable() {
                @Override public void run() {
                    Dados.edit().putBoolean("home_shortcuts_visible", !visible).apply();
                    sheet.dismiss(); refreshHomeShortcuts();
                }
            });
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        sheet.setContentView(scroll); sheet.show();
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
    }

    private void addShortcutVisibilitySwitch(LinearLayout parent, String label, final String key) {
        MaterialSwitch toggle = new MaterialSwitch(this);
        toggle.setText(label);
        toggle.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        toggle.setTextSize(14);
        toggle.setPadding(dp(12), 0, dp(12), 0);
        toggle.setChecked(Dados.getBoolean(key, true));
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton button, boolean checked) {
                Dados.edit().putBoolean(key, checked).apply();
                refreshHomeShortcuts();
            }
        });
        parent.addView(toggle, new LinearLayout.LayoutParams(-1, dp(56)));
    }

    private void showShortcutEditor(final BrowserLibraryStore.Entry entry) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(8), dp(24), 0);
        final TextInputLayout nameLayout = new TextInputLayout(this);
        nameLayout.setHint("Nome");
        final EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(512) });
        if (entry != null) name.setText(entry.title);
        nameLayout.addView(name, new LinearLayout.LayoutParams(-1, -2));
        content.addView(nameLayout, new LinearLayout.LayoutParams(-1, -2));
        final TextInputLayout urlLayout = new TextInputLayout(this);
        urlLayout.setHint("Endereço HTTPS");
        final EditText url = new EditText(this);
        url.setSingleLine(true);
        url.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        url.setFilters(new android.text.InputFilter[] { new android.text.InputFilter.LengthFilter(8192) });
        if (entry != null) url.setText(entry.url);
        urlLayout.addView(url, new LinearLayout.LayoutParams(-1, -2));
        content.addView(urlLayout, new LinearLayout.LayoutParams(-1, -2));
        final androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(entry == null ? "Adicionar site" : "Editar página fixada")
            .setView(content).setNegativeButton("Cancelar", null).setPositiveButton("Salvar", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface ignored) {
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        String value = url.getText().toString().trim();
                        if (!value.contains("://") && isDomainOrIp(value)) value = "https://" + value;
                        if (value.matches(".*\\s+.*") || !isSecureOrigin(value)) {
                            urlLayout.setError("Insira um endereço HTTPS válido"); return;
                        }
                        urlLayout.setError(null);
                        boolean saved = entry == null ? browserLibraryStore.pin(name.getText().toString(), value)
                            : browserLibraryStore.updatePinned(entry.url, name.getText().toString(), value);
                        if (!saved) {
                            urlLayout.setError(entry == null ? "Limite de 4 páginas ou endereço inválido" : "Endereço inválido ou já fixado");
                            return;
                        }
                        Dados.edit().putBoolean("home_shortcuts_visible", true).putBoolean("home_show_pinned", true).apply();
                        dialog.dismiss(); refreshHomeShortcuts();
                        Toast.makeText(MainActivity.this, entry == null ? "Página fixada" : "Página atualizada", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    private void showWebContextMenu(int x, int y, GeckoSession.ContentDelegate.ContextElement element) {
        if (element == null || browserContentContainer == null || !hasPageForActions()) return;
        final String link = isSecureOrigin(element.linkUri) ? element.linkUri : null;
        final String image = element.type == GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE
            && isSecureOrigin(element.srcUri) ? element.srcUri : null;
        if (link == null && image == null) return;
        View anchor = new View(this);
        int[] location = new int[2];
        browserContentContainer.getLocationOnScreen(location);
        FrameLayout.LayoutParams position = new FrameLayout.LayoutParams(dp(1), dp(1));
        position.leftMargin = Math.max(0, Math.min(browserContentContainer.getWidth() - 1, x - location[0]));
        position.topMargin = Math.max(0, Math.min(browserContentContainer.getHeight() - 1, y - location[1]));
        browserContentContainer.addView(anchor, position);
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        if (link != null) {
            popup.getMenu().add("Abrir link em nova aba").setOnMenuItemClickListener(item -> { createBrowserTab(link); return true; });
            popup.getMenu().add("Copiar endereço do link").setOnMenuItemClickListener(item -> { copyContextAddress(link); return true; });
            popup.getMenu().add("Compartilhar link").setOnMenuItemClickListener(item -> { shareContextAddress(link); return true; });
        }
        if (image != null) {
            popup.getMenu().add("Abrir imagem").setOnMenuItemClickListener(item -> { loadUrlWithPrivacyHeaders(image); return true; });
            popup.getMenu().add("Salvar imagem").setOnMenuItemClickListener(item -> { downloadShortcut(image); return true; });
            popup.getMenu().add("Compartilhar imagem").setOnMenuItemClickListener(item -> { shareContextImage(image); return true; });
        }
        popup.setOnDismissListener(menu -> browserContentContainer.removeView(anchor));
        anchor.post(() -> {
            if (!isFinishing() && !isDestroyed() && anchor.isAttachedToWindow()) popup.show();
            else browserContentContainer.removeView(anchor);
        });
    }

    private void copyContextAddress(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("Endereço", url));
        showFeedback("Endereço copiado", null, null);
    }

    private void shareContextAddress(String url) {
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url);
        try { startActivity(Intent.createChooser(send, "Compartilhar")); }
        catch (ActivityNotFoundException e) { showFeedback("Nenhum aplicativo disponível", null, null); }
    }

    private void shareContextImage(String url) {
        if (!isSecureOrigin(url) || geckoEngine == null) return;
        try {
            new GeckoWebExecutor(GeckoEngineController.getRuntime())
                .fetch(new WebRequest.Builder(url).build(), GeckoWebExecutor.FETCH_FLAGS_NO_REDIRECTS)
                .accept(response -> {
                    if (isFinishing() || isDestroyed()) { closeResponseBody(response); return; }
                    String contentType = responseHeader(response, "Content-Type");
                    final String mime = contentType == null ? "" : contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
                    if (response == null || response.body == null || response.statusCode < 200 || response.statusCode >= 300
                        || !isSecureOrigin(response.uri) || !mime.startsWith("image/")) {
                        closeResponseBody(response); showFeedback("Não foi possível compartilhar a imagem", null, null); return;
                    }
                    response.setReadTimeoutMillis(30000);
                    Context app = getApplicationContext();
                    java.lang.ref.WeakReference<MainActivity> owner = new java.lang.ref.WeakReference<>(this);
                    new Thread(() -> {
                        java.io.File file = null;
                        boolean success = false;
                        try (java.io.InputStream in = response.body) {
                            java.io.File directory = new java.io.File(app.getCacheDir(), "shared_images");
                            if (!directory.exists() && !directory.mkdirs()) throw new IOException("Cache indisponível");
                            java.io.File[] previous = directory.listFiles();
                            if (previous != null) for (java.io.File old : previous)
                                if (System.currentTimeMillis() - old.lastModified() > 24L * 60 * 60 * 1000) old.delete();
                            String extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
                            if (extension == null || !extension.matches("[a-zA-Z0-9]{1,8}")) extension = "img";
                            file = java.io.File.createTempFile("snav-", "." + extension, directory);
                            try (java.io.OutputStream out = new java.io.FileOutputStream(file)) {
                                byte[] buffer = new byte[32768]; int count; long total = 0;
                                while ((count = in.read(buffer)) != -1) {
                                    total += count;
                                    if (total > 20L * 1024 * 1024) throw new IOException("Imagem muito grande");
                                    out.write(buffer, 0, count);
                                }
                            }
                            success = true;
                        } catch (IOException | RuntimeException ignored) {
                            if (file != null) file.delete();
                        }
                        final java.io.File ready = file;
                        final boolean complete = success;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            MainActivity activity = owner.get();
                            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                                if (ready != null) ready.delete(); return;
                            }
                            if (!complete) { activity.showFeedback("Não foi possível compartilhar a imagem", null, null); return; }
                            try {
                                Uri content = androidx.core.content.FileProvider.getUriForFile(app, app.getPackageName() + ".fileprovider", ready);
                                Intent send = new Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, content)
                                    .setClipData(ClipData.newRawUri("Imagem", content))
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(Intent.createChooser(send, "Compartilhar imagem"));
                            } catch (IllegalArgumentException | ActivityNotFoundException e) {
                                ready.delete(); activity.showFeedback("Não foi possível compartilhar a imagem", null, null);
                            }
                        });
                    }, "SnavImageShare").start();
                }, error -> { if (!isFinishing() && !isDestroyed()) showFeedback("Não foi possível compartilhar a imagem", null, null); });
        } catch (IllegalArgumentException | IllegalStateException e) {
            showFeedback("Não foi possível compartilhar a imagem", null, null);
        }
    }

    private void installSelectionActions() {
        geckoEngine.getSession().setSelectionActionDelegate(new org.mozilla.geckoview.BasicSelectionActionDelegate(this) {
            private static final int SEARCH = 0x534e41;
            @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
                boolean changed = super.onPrepareActionMode(mode, menu);
                boolean canSearch = mSelection != null && !mSelection.text.trim().isEmpty()
                    && (mSelection.flags & GeckoSession.SelectionActionDelegate.FLAG_IS_PASSWORD) == 0;
                if (canSearch && menu.findItem(SEARCH) == null) { menu.add(0, SEARCH, 1000, "Pesquisar"); changed = true; }
                if (!canSearch && menu.findItem(SEARCH) != null) { menu.removeItem(SEARCH); changed = true; }
                return changed;
            }
            @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
                if (item.getItemId() == SEARCH) {
                    if (mSelection != null && (mSelection.flags & GeckoSession.SelectionActionDelegate.FLAG_IS_PASSWORD) == 0) {
                        String query = mSelection.text;
                        mode.finish();
                        loadUrlWithPrivacyHeaders(buildSearchUrl(query));
                    }
                    return true;
                }
                return super.onActionItemClicked(mode, item);
            }
        });
    }

    private void downloadShortcut(final String url) {
        if (!isSecureOrigin(url)) return;
        showSnavDecisionSheet("Baixar link?", "O site receberá uma solicitação para baixar este endereço",
            "Continuar", "Cancelar", new Runnable() {
                @Override public void run() {
                    try {
                        WebRequest.Builder builder = new WebRequest.Builder(url);
                        if (Dados.getBoolean("do_not_track", false)) builder.header("DNT", "1");
                        new GeckoWebExecutor(GeckoEngineController.getRuntime())
                            .fetch(builder.build(), GeckoWebExecutor.FETCH_FLAGS_NO_REDIRECTS).accept(
                                new GeckoResult.Consumer<WebResponse>() {
                                    @Override public void accept(WebResponse response) {
                                        if (isFinishing() || isDestroyed()) { closeResponseBody(response); return; }
                                        handleGeckoDownload(response);
                                    }
                                }, new GeckoResult.Consumer<Throwable>() {
                                    @Override public void accept(Throwable error) {
                                        if (!isFinishing() && !isDestroyed())
                                            Toast.makeText(MainActivity.this, "Não foi possível baixar o link", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } catch (IllegalArgumentException | IllegalStateException error) {
                        Toast.makeText(MainActivity.this, "Não foi possível baixar o link", Toast.LENGTH_LONG).show();
                    }
                }
            }, null);
    }

    // Request only the visited origin's conventional icon, without cookies or redirects.
    // A Material symbol remains visible when a site has no decodable icon.
    private void bindFavicon(final ImageView image, String pageUrl) {
        if (!isSecureOrigin(pageUrl)) return;
        Uri page = Uri.parse(pageUrl);
        final String iconUrl = new Uri.Builder().scheme("https").encodedAuthority(page.getEncodedAuthority()).path("/favicon.ico").build().toString();
        Bitmap cached = faviconCache.get(iconUrl);
        if (cached != null) { image.setImageTintList(null); image.setImageBitmap(cached); return; }
        if (faviconRequests.contains(iconUrl) || faviconAttempted.contains(iconUrl) || faviconExecutor.isShutdown()) return;
        faviconRequests.add(iconUrl);
        faviconExecutor.execute(new Runnable() {
            @Override public void run() {
                Bitmap result = null;
                java.net.HttpURLConnection connection = null;
                try {
                    connection = (java.net.HttpURLConnection) new java.net.URL(iconUrl).openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Cookie", "");
                    if (Dados.getBoolean("do_not_track", false)) connection.setRequestProperty("DNT", "1");
                    if (connection.getResponseCode() == 200 && connection.getContentLength() <= 262144) {
                        try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[4096]; int count, total = 0;
                            while ((count = input.read(buffer)) != -1 && (total += count) <= 262144) output.write(buffer, 0, count);
                            if (total <= 262144) {
                                byte[] bytes = extractFaviconImage(output.toByteArray());
                                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
                                if (bounds.outWidth > 0 && bounds.outHeight > 0 && bounds.outWidth <= 1024 && bounds.outHeight <= 1024) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = Math.max(1, Math.max(bounds.outWidth, bounds.outHeight) / 64);
                                    result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                }
                            }
                        }
                    }
                } catch (IOException | RuntimeException ignored) {
                } finally { if (connection != null) connection.disconnect(); }
                final Bitmap icon = result;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        faviconRequests.remove(iconUrl);
                        faviconAttempted.add(iconUrl);
                        if (isFinishing() || isDestroyed()) return;
                        if (icon != null) { faviconCache.put(iconUrl, icon); refreshHomeShortcuts(); }
                    }
                });
            }
        });
    }

    private byte[] extractFaviconImage(byte[] bytes) {
        // Android may not decode ICO containers; use an embedded PNG when present.
        if (bytes.length < 6 || bytes[0] != 0 || bytes[1] != 0 || bytes[2] != 1 || bytes[3] != 0) return bytes;
        int count = (bytes[4] & 255) | ((bytes[5] & 255) << 8);
        for (int i = 0; i < count && 6 + i * 16 + 16 <= bytes.length; i++) {
            int entry = 6 + i * 16;
            long size = littleEndianInt(bytes, entry + 8);
            long offset = littleEndianInt(bytes, entry + 12);
            if (size >= 8 && offset >= 0 && offset + size <= bytes.length
                && (bytes[(int) offset] & 255) == 0x89 && bytes[(int) offset + 1] == 'P'
                && bytes[(int) offset + 2] == 'N' && bytes[(int) offset + 3] == 'G') {
                return Arrays.copyOfRange(bytes, (int) offset, (int) (offset + size));
            }
        }
        return bytes;
    }

    private long littleEndianInt(byte[] data, int offset) {
        return (data[offset] & 255L) | ((data[offset + 1] & 255L) << 8)
            | ((data[offset + 2] & 255L) << 16) | ((data[offset + 3] & 255L) << 24);
    }

    private void restorePersistedBrowserTabs() {
        if (!browserTabs.isEmpty() || Dados == null) return;
        try {
            JSONArray saved = new JSONArray(Dados.getString("browser_tabs_v1", "[]"));
            for (int i = 0; i < saved.length() && i < 12; i++) {
                JSONObject item = saved.optJSONObject(i);
                if (item == null) continue;
                String url = item.optString("url", "");
                if (!url.isEmpty() && !url.equals("about:blank")
                    && !url.startsWith("https://")) continue;
                browserTabs.add(new BrowserTab(item.optString("title", "Nova aba"),
                    url.isEmpty() ? null : url, null));
            }
            if (!browserTabs.isEmpty()) activeBrowserTab = Math.max(0,
                Math.min(Dados.getInt("browser_active_tab_v1", 0), browserTabs.size() - 1));
        } catch (JSONException error) {
            logDiagnostic("Não foi possível restaurar as abas salvas");
            browserTabs.clear();
        }
    }

    private void persistBrowserTabs() {
        if (Dados == null || browserTabs.isEmpty()) return;
        JSONArray saved = new JSONArray();
        for (BrowserTab tab : browserTabs) {
            JSONObject item = new JSONObject();
            try {
                item.put("title", tab.title == null ? "Nova aba" : tab.title);
                item.put("url", tab.url == null ? "" : tab.url);
                saved.put(item);
            } catch (JSONException ignored) {}
        }
        Dados.edit().putString("browser_tabs_v1", saved.toString())
            .putInt("browser_active_tab_v1", activeBrowserTab).apply();
    }

    private void updateTabCounter() { if (tabCounterButton != null) tabCounterButton.setText(String.valueOf(Math.max(1, browserTabs.size()))); }

    private void captureActiveTabState() {
        if (geckoEngine == null || browserTabs.isEmpty()) return;
        geckoEngine.flushSessionState();
        BrowserTab active = browserTabs.get(activeBrowserTab);
        GeckoSession.SessionState state = geckoEngine.getSessionState();
        if (state != null) active.state = new GeckoSession.SessionState(state);
        active.url = geckoEngine.getCurrentUri(); active.title = currentPageTitle;
    }

    private void switchBrowserTab(int index) {
        if (geckoEngine == null || index < 0 || index >= browserTabs.size()) return;
        if (index == activeBrowserTab) { showScreen(browserTabs.get(index).url == null ? 1 : 2); return; }
        captureActiveTabState(); activeBrowserTab = index;
        if (extensionManager != null) extensionManager.onPageChanged();
        BrowserTab target = browserTabs.get(index);
        exitReaderMode();
        if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
        if (target.state != null) geckoEngine.restoreState(target.state, target.url);
        else if (target.url != null && target.url.startsWith("https://")) loadUrlWithPrivacyHeaders(target.url);
        else geckoEngine.loadUri("about:blank", false);
        currentPageTitle = target.title; text_barra.setText(domainForAddressBar(target.url));
        vazio.setVisibility(target.url == null || target.url.isEmpty() || "about:blank".equals(target.url) ? View.VISIBLE : View.GONE);
        updateTabCounter(); persistBrowserTabs();
        showScreen(target.url == null || "about:blank".equals(target.url) ? 1 : 2);
    }

    private void createBrowserTab() { createBrowserTab(null); }

    private void createBrowserTab(String url) {
        if (browserTabs.size() >= 12) { Toast.makeText(this, "Você pode abrir até 12 abas", Toast.LENGTH_SHORT).show(); return; }
        browserTabs.add(new BrowserTab("Nova aba", url, null));
        int next = browserTabs.size() - 1;
        switchBrowserTab(next);
    }

    private void closeBrowserTab(int index) {
        if (browserTabs.size() <= 1) {
            exitReaderMode();
            if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
            BrowserTab only = browserTabs.get(0); only.title = "Nova aba"; only.url = null; only.state = null;
            currentPageTitle = null; activeBrowserTab = 0; text_barra.setText("Snav Browser"); vazio.setVisibility(View.VISIBLE); if (geckoEngine != null) geckoEngine.loadUri("about:blank", false);
        } else {
            boolean closingActive = index == activeBrowserTab;
            if (closingActive) captureActiveTabState();
            browserTabs.remove(index);
            if (index < activeBrowserTab) activeBrowserTab--;
            else if (closingActive) activeBrowserTab = Math.min(index, browserTabs.size() - 1);
            if (closingActive) {
                exitReaderMode();
                if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
                BrowserTab selected = browserTabs.get(activeBrowserTab);
                if (selected.state != null) geckoEngine.restoreState(selected.state, selected.url);
                else if (selected.url != null && selected.url.startsWith("https://")) loadUrlWithPrivacyHeaders(selected.url);
                else geckoEngine.loadUri("about:blank", false);
                currentPageTitle = selected.title;
                text_barra.setText(domainForAddressBar(selected.url));
                vazio.setVisibility(selected.url == null || selected.url.isEmpty() || "about:blank".equals(selected.url) ? View.VISIBLE : View.GONE);
            }
        }
        updateTabCounter(); persistBrowserTabs(); updateTopBar(); showTabsBottomSheet();
    }

    private void performLoadStop() {
        if (pageLoading && geckoEngine != null) {
            stoppedByUser = true;
            loadingPageUrl = null;
            geckoEngine.stop();
            finishPageProgress();
            updateTopBar();
        } else if (geckoEngine != null && geckoEngine.getCurrentUri() != null && !geckoEngine.getCurrentUri().equals("about:blank")) {
            if (readerMode) exitReaderMode();
            geckoEngine.reload();
        } else {
            openSearchScreen();
        }
    }

    private void showMenuBottomSheet() {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        root.addView(new BottomSheetDragHandleView(this), new LinearLayout.LayoutParams(-1, dp(42)));
        TextView heading = new TextView(this);
        heading.setText("Menu");
        heading.setTextSize(21);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        root.addView(heading);
        addMenuSheetRow(root, R.drawable.ms_tab, "Gerenciar abas", true, new Runnable() {
            @Override public void run() { sheet.dismiss(); showTabsBottomSheet(); }
        });
        addMenuSheetRow(root, R.drawable.ms_menu_book, "Biblioteca", true, new Runnable() {
            @Override public void run() { sheet.dismiss(); showBrowserLibraryBottomSheet(false); }
        });
        final boolean pageAvailable = hasPageForActions();
        boolean canStop = tela2.getVisibility() == View.VISIBLE && pageLoading
            && isSecureOrigin(loadingPageUrl);
        addMenuSheetRow(root, pageLoading ? R.drawable.ms_stop : R.drawable.ms_refresh,
            pageLoading ? "Interromper carregamento" : "Recarregar página", pageAvailable || canStop, new Runnable() {
            @Override public void run() { sheet.dismiss(); performLoadStop(); }
        });
        final String currentUrl = pageAvailable ? geckoEngine.getCurrentUri() : null;
        boolean pinned = false;
        if (currentUrl != null) {
            for (BrowserLibraryStore.Entry entry : browserLibraryStore.getPinned()) {
                if (entry.url.equals(currentUrl)) pinned = true;
            }
        }
        final boolean alreadyPinned = pinned;
        addMenuSheetRow(root, pinned ? R.drawable.ms_keep_off : R.drawable.ms_keep,
            pinned ? "Desafixar da tela inicial" : "Fixar na tela inicial", pageAvailable, new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                if (!hasPageForActions() || !currentUrl.equals(geckoEngine.getCurrentUri())) return;
                if (alreadyPinned) browserLibraryStore.unpin(currentUrl);
                else if (!browserLibraryStore.pin(currentPageTitle, currentUrl)) {
                    Toast.makeText(MainActivity.this, "Fixe até 4 páginas", Toast.LENGTH_SHORT).show();
                    return;
                }
                refreshHomeShortcuts();
                Toast.makeText(MainActivity.this, alreadyPinned ? "Página desafixada" : "Página fixada", Toast.LENGTH_SHORT).show();
            }
        });
        addMenuSheetRow(root, R.drawable.ms_bookmark_add, "Salvar nos favoritos", pageAvailable, new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                if (!hasPageForActions() || !currentUrl.equals(geckoEngine.getCurrentUri())) return;
                if (browserLibraryStore.addBookmark(currentPageTitle, currentUrl))
                    showFeedback("Salvo nos favoritos", null, null);
                else Toast.makeText(MainActivity.this, "Não foi possível salvar nos favoritos", Toast.LENGTH_SHORT).show();
            }
        });
        addMenuSheetRow(root, R.drawable.ms_content_copy, "Copiar link", pageAvailable, new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                if (!hasPageForActions() || !currentUrl.equals(geckoEngine.getCurrentUri())) return;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Link", currentUrl));
                    showFeedback("Link copiado", null, null);
                }
            }
        });
        addMenuSheetRow(root, R.drawable.ms_translate, "Traduzir página", pageAvailable, new Runnable() {
            @Override public void run() { sheet.dismiss(); if (hasPageForActions()) showTranslationBottomSheet(); }
        });
        addMenuSheetRow(root, R.drawable.ms_text_snippet, "Modo de leitura", pageAvailable, new Runnable() {
            @Override public void run() { sheet.dismiss(); if (hasPageForActions()) showReaderOptionsBottomSheet(); }
        });
        addMenuSheetRow(root, R.drawable.ms_open_in_browser, "Abrir em outro navegador", pageAvailable, new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                if (!hasPageForActions() || !currentUrl.equals(geckoEngine.getCurrentUri())) return;
                Intent external = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
                try { startActivity(external); }
                catch (ActivityNotFoundException error) { Toast.makeText(MainActivity.this, "Nenhum navegador disponível", Toast.LENGTH_SHORT).show(); }
            }
        });
        addMenuSheetRow(root, R.drawable.ms_add_box, "Configurar atalhos", true, new Runnable() {
            @Override public void run() { sheet.dismiss(); showShortcutMenu(null, false); }
        });
        addMenuSheetRow(root, R.drawable.ms_build, "Ferramentas", true, new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                toolsOpenedFromBrowser = tela2.getVisibility() == View.VISIBLE;
                showScreen(3);
            }
        });
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        sheet.setContentView(scroll);
        sheet.show();
        View sheetSurface = sheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheetSurface != null) sheetSurface.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface)));
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
    }

    private void addMenuSheetRow(LinearLayout parent, int iconRes, String label, boolean enabled, final Runnable action) {
        MaterialButton row = new MaterialButton(this, null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle);
        row.setAllCaps(false);
        row.setText(label);
        row.setTextSize(14);
        row.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        row.setIconResource(iconRes);
        row.setIconTint(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant)));
        row.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        row.setStrokeWidth(0);
        row.setCornerRadius(dp(16));
        row.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface)));
        row.setEnabled(enabled);
        row.setAlpha(enabled ? 1f : 0.38f);
        if (!enabled) row.setContentDescription(label + (label.contains("privada") ? ". Indisponível" : ". Abra uma página para usar esta ação"));
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { action.run(); }
        });
        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(56)));
    }

    private void setupAdaptiveWidths() {
        linear1.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left <= 0) return;
            int homeWidth = Math.max(1, Math.min(dp(600), right - left - dp(48)));
            resizeCentered(linear_barra_pesquisa, homeWidth);
            resizeCentered((View) homeShortcuts.getParent(), homeWidth);
            View tools = findViewById(R.id.linear33);
            android.view.ViewGroup.LayoutParams params = tools.getLayoutParams();
            int toolsWidth = Math.min(dp(720), right - left);
            if (params.width != toolsWidth) {
                params.width = toolsWidth;
                if (params instanceof FrameLayout.LayoutParams) ((FrameLayout.LayoutParams) params).gravity = Gravity.CENTER_HORIZONTAL;
                tools.setLayoutParams(params);
            }
        });
    }

    private void resizeCentered(View view, int width) {
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params.width != width) { params.width = width; view.setLayoutParams(params); }
    }

    private void setupToolDestinations() {
        LinearLayout tools = findViewById(R.id.linear33);
        addToolDestination(tools, findViewById(R.id.tools_appearance), "Aparência");
        addToolDestination(tools, findViewById(R.id.tools_privacy), "Privacidade e conteúdo");
    }

    private void addToolDestination(LinearLayout parent, LinearLayout contents, String title) {
        int index = parent.indexOfChild(contents);
        if (index < 0) return;
        View oldHeading = index > 0 ? parent.getChildAt(index - 1) : null;
        if (oldHeading instanceof TextView) oldHeading.setVisibility(View.GONE);
        contents.setVisibility(View.GONE);
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(title);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setMinHeight(dp(56));
        parent.addView(button, index);
        button.setOnClickListener(v -> {
            int originalIndex = parent.indexOfChild(contents);
            if (originalIndex < 0) return;
            parent.removeView(contents);
            contents.setVisibility(View.VISIBLE);
            BrowserPanel panel = new BrowserPanel(title);
            ScrollView scroll = new ScrollView(this);
            scroll.setFillViewport(false);
            contents.setPadding(dp(20), dp(12), dp(20), dp(24));
            scroll.addView(contents, new ScrollView.LayoutParams(-1, -2));
            panel.setContentView(scroll);
            panel.setOnDismissListener(ignored -> {
                scroll.removeView(contents);
                contents.setVisibility(View.GONE);
                parent.addView(contents, Math.min(originalIndex, parent.getChildCount()));
            });
            panel.show();
        });
    }

    private BrowserPanel activePanel;

    /** A normal in-activity destination, preserving the Gecko session beneath it. */
    private final class BrowserPanel {
        final LinearLayout frame = new LinearLayout(MainActivity.this);
        Runnable onDismiss;
        BrowserPanel(String title) {
            frame.setOrientation(LinearLayout.VERTICAL);
            frame.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface));
            frame.setClickable(true);
            com.google.android.material.appbar.MaterialToolbar toolbar = new com.google.android.material.appbar.MaterialToolbar(MainActivity.this);
            toolbar.setTitle(title);
            toolbar.setNavigationIcon(R.drawable.ms_arrow_back);
            toolbar.setNavigationContentDescription("Voltar");
            toolbar.setNavigationOnClickListener(v -> dismiss());
            frame.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(56)));
        }
        void setContentView(View content) {
            FrameLayout container = new FrameLayout(MainActivity.this);
            FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(-1, -1, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            container.addView(content, contentParams);
            container.addOnLayoutChangeListener((view, l, t, r, b, ol, ot, oright, ob) -> {
                int width = Math.min(dp(720), r - l);
                if (width > 0 && contentParams.width != width) { contentParams.width = width; content.setLayoutParams(contentParams); }
            });
            frame.addView(container, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        void setOnDismissListener(android.content.DialogInterface.OnDismissListener listener) {
            onDismiss = () -> listener.onDismiss(null);
        }
        void show() {
            if (activePanel != null) activePanel.dismiss();
            activePanel = this;
            android.view.ViewGroup host = findViewById(R.id.snav_snackbar_host);
            if (host.getChildCount() > 0) host.getChildAt(0).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            host.addView(frame,
                new android.view.ViewGroup.LayoutParams(-1, -1));
            frame.setFocusableInTouchMode(true);
            frame.requestFocus();
        }
        boolean isShowing() { return activePanel == this; }
        void dismiss() {
            if (activePanel != this) return;
            activePanel = null;
            if (frame.getParent() instanceof android.view.ViewGroup) {
                android.view.ViewGroup host = (android.view.ViewGroup) frame.getParent();
                host.removeView(frame);
                if (host.getChildCount() > 0) host.getChildAt(0).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
            }
            if (onDismiss != null) onDismiss.run();
        }
    }

    private void showTabsBottomSheet() {
        final BrowserPanel sheet = new BrowserPanel("Abas abertas");
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22), dp(18), dp(22), dp(24));
        TextView heading = new TextView(this); heading.setText("Abas abertas · " + browserTabs.size()); heading.setTextSize(21); heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD); heading.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface)); root.addView(heading);
        addLibraryAction(root, "Nova aba", new Runnable() { @Override public void run() { sheet.dismiss(); createBrowserTab(); } });
        ScrollView scroll = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        for (int i = 0; i < browserTabs.size(); i++) {
            final int index = i; final BrowserTab tab = browserTabs.get(i);
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView label = new TextView(this); String name = tab.title == null || tab.title.trim().isEmpty() ? "Nova aba" : tab.title;
            label.setText((i == activeBrowserTab ? "Aba atual · " : "") + name + "\n" + (tab.url == null ? "Nenhuma página aberta" : domainForAddressBar(tab.url)));
            label.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface)); label.setTextSize(14); label.setPadding(dp(8), dp(14), dp(8), dp(14));
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            ImageButton close = new ImageButton(this);
            close.setImageResource(R.drawable.ms_close);
            close.setImageTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant)));
            close.setBackgroundResource(android.R.drawable.list_selector_background);
            close.setPadding(dp(12), dp(12), dp(12), dp(12));
            close.setContentDescription("Fechar " + name);
            row.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
            label.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sheet.dismiss(); switchBrowserTab(index); } });
            close.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sheet.dismiss(); closeBrowserTab(index); } });
            list.addView(row);
        }
        sheet.setContentView(root); sheet.show();
    }

    private void addLibraryAction(LinearLayout parent, String label, final Runnable action) {
        TextView button = new TextView(this);
        button.setText(label);
        button.setTextColor(themeColor(com.google.android.material.R.attr.colorSecondary, R.color.snav_primary));
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setMinHeight(dp(52));
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        button.setBackgroundResource(android.R.drawable.list_selector_background);
        button.setClickable(true);
        button.setFocusable(true);
        parent.addView(button, new LinearLayout.LayoutParams(-1, -2));
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { action.run(); }
        });
    }

    private String historyDateLabel(long timestamp) {
        Calendar target = Calendar.getInstance(); target.setTimeInMillis(timestamp);
        Calendar today = Calendar.getInstance();
        if (target.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) return "Hoje";
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (target.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) return "Ontem";
        return new SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.getDefault()).format(new Date(timestamp));
    }

    private void showBrowserLibraryBottomSheet(final boolean historyMode) {
        final BrowserPanel sheet = new BrowserPanel("Biblioteca");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView favorites = new TextView(this);
        favorites.setText("Favoritos");
        TextView history = new TextView(this);
        history.setText("Histórico");
        for (TextView tab : new TextView[] { favorites, history }) {
            tab.setTextSize(14);
            tab.setTextColor(themeColor(com.google.android.material.R.attr.colorSecondary, R.color.snav_primary));
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(12), dp(14), dp(12), dp(14));
            actions.addView(tab, new LinearLayout.LayoutParams(0, dp(48), 1));
        }
        root.addView(actions);
        final LinearLayout entries = new LinearLayout(this);
        entries.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout libraryContent = new LinearLayout(this);
        libraryContent.setOrientation(LinearLayout.VERTICAL);
        libraryContent.addView(entries);
        scroll.addView(libraryContent, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        final Runnable[] render = new Runnable[1];
        final boolean[] showingHistory = new boolean[] { historyMode };
        render[0] = new Runnable() {
            @Override public void run() {
                entries.removeAllViews();
                List<BrowserLibraryStore.Entry> data = showingHistory[0]
                    ? browserLibraryStore.getHistory() : browserLibraryStore.getBookmarks();
                if (data.isEmpty()) {
                    TextView empty = new TextView(MainActivity.this);
                    empty.setText(showingHistory[0] ? "Seu histórico está vazio" : "Nenhum favorito salvo");
                    empty.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
                    empty.setPadding(0, dp(22), 0, dp(22));
                    entries.addView(empty);
                }
                String lastHistoryGroup = null;
                for (final BrowserLibraryStore.Entry item : data) {
                    if (showingHistory[0]) {
                        String group = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(item.visitedAt));
                        if (!group.equals(lastHistoryGroup)) {
                            TextView groupHeading = new TextView(MainActivity.this);
                            groupHeading.setText(historyDateLabel(item.visitedAt));
                            groupHeading.setTextColor(themeColor(com.google.android.material.R.attr.colorSecondary, R.color.snav_primary));
                            groupHeading.setTextSize(13);
                            groupHeading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                            groupHeading.setPadding(dp(8), dp(16), dp(8), dp(6));
                            entries.addView(groupHeading);
                            lastHistoryGroup = group;
                        }
                    }
                    LinearLayout row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setPadding(dp(8), dp(12), dp(8), dp(12));
                    TextView name = new TextView(MainActivity.this);
                    name.setText(item.title);
                    name.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
                    name.setTextSize(15);
                    name.setMaxLines(1);
                    TextView address = new TextView(MainActivity.this);
                    address.setText(item.url);
                    address.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
                    address.setTextSize(12);
                    address.setMaxLines(1);
                    row.addView(name);
                    row.addView(address);
                    row.setBackgroundResource(android.R.drawable.list_selector_background);
                    row.setClickable(true);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View view) {
                            sheet.dismiss();
                            if (geckoEngine != null) { loadUrlWithPrivacyHeaders(item.url); showScreen(2); vazio.setVisibility(View.GONE); }
                        }
                    });
                    row.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override public boolean onLongClick(View view) {
                            boolean removed = showingHistory[0] ? browserLibraryStore.removeHistory(item.url) : browserLibraryStore.removeBookmark(item.url);
                            if (removed) {
                                render[0].run();
                                Toast.makeText(MainActivity.this, showingHistory[0] ? "Página removida do histórico" : "Favorito removido", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            return false;
                        }
                    });
                    entries.addView(row, new LinearLayout.LayoutParams(-1, -2));
                }
            }
        };
        favorites.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showingHistory[0] = false; render[0].run(); } });
        history.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showingHistory[0] = true; render[0].run(); } });
        addLibraryAction(libraryContent, "Gerenciador de downloads", new Runnable() {
            @Override public void run() { sheet.dismiss(); showDownloadsBottomSheet(); }
        });
        addLibraryAction(libraryContent, "Exportar favoritos", new Runnable() {
            @Override public void run() { exportBookmarks(); }
        });
        addLibraryAction(libraryContent, "Importar favoritos", new Runnable() {
            @Override public void run() { importBookmarks(); }
        });
        addLibraryAction(libraryContent, "Limpar histórico", new Runnable() {
            @Override public void run() {
                browserLibraryStore.clearHistory();
                refreshHomeShortcuts();
                showingHistory[0] = true;
                render[0].run();
                Toast.makeText(MainActivity.this, "Histórico limpo", Toast.LENGTH_SHORT).show();
            }
        });
        render[0].run();
        sheet.setContentView(root);
        sheet.show();
    }

    private void showDownloadsBottomSheet() {
        final BrowserPanel sheet = new BrowserPanel("Downloads");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        final TextView progress = new TextView(this);
        progress.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
        progress.setTextSize(14);
        root.addView(progress, new LinearLayout.LayoutParams(-1, -2));
        final Handler progressHandler = new Handler(Looper.getMainLooper());
        final Runnable[] updateProgress = new Runnable[1];
        updateProgress[0] = () -> {
            StringBuilder text = new StringBuilder();
            for (DownloadStatus status : ACTIVE_DOWNLOADS.values()) {
                if (text.length() > 0) text.append("\n");
                text.append(status.name).append(" · ");
                if (status.total > 0) text.append(Math.min(100, (int) (status.bytes * 100.0 / status.total))).append("%");
                else text.append(android.text.format.Formatter.formatShortFileSize(this, status.bytes));
            }
            progress.setText(text);
            progress.setVisibility(text.length() == 0 ? View.GONE : View.VISIBLE);
            if (sheet.isShowing()) progressHandler.postDelayed(updateProgress[0], 500);
        };
        sheet.setOnDismissListener(dialog -> progressHandler.removeCallbacks(updateProgress[0]));
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (Build.VERSION.SDK_INT >= 29) {
            android.database.Cursor cursor = null;
            try {
                String[] projection = {
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED
                };
                cursor = getContentResolver().query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                    while (cursor.moveToNext()) {
                        final Uri itemUri = ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn));
                        String name = cursor.getString(nameColumn);
                        long size = cursor.getLong(sizeColumn);
                        addDownloadFileRow(list, itemUri, name, size);
                    }
                }
            } catch (Exception error) {
                logDiagnostic("Falha ao consultar downloads: " + error.getClass().getSimpleName());
            } finally {
                if (cursor != null) cursor.close();
            }
        } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.database.Cursor legacyCursor = null;
            try {
                Uri filesUri = MediaStore.Files.getContentUri("external");
                String[] projection = { MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
                String selection = MediaStore.MediaColumns.DATA + " LIKE ?";
                String[] selectionArgs = { "%" + java.io.File.separator + Environment.DIRECTORY_DOWNLOADS + java.io.File.separator + "%" };
                legacyCursor = getContentResolver().query(filesUri, projection, selection, selectionArgs,
                    MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
                if (legacyCursor != null) {
                    int idColumn = legacyCursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                    int nameColumn = legacyCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    int sizeColumn = legacyCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
                    while (legacyCursor.moveToNext()) {
                        Uri itemUri = ContentUris.withAppendedId(filesUri, legacyCursor.getLong(idColumn));
                        addDownloadFileRow(list, itemUri, legacyCursor.getString(nameColumn), legacyCursor.getLong(sizeColumn));
                    }
                }
            } catch (Exception error) {
                logDiagnostic("Falha ao consultar downloads legados: " + error.getClass().getSimpleName());
            } finally {
                if (legacyCursor != null) legacyCursor.close();
            }
        } else {
            TextView legacy = new TextView(this);
            legacy.setText("Para ver downloads, permita o acesso aos arquivos");
            legacy.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
            legacy.setPadding(0, dp(18), 0, dp(18));
            legacy.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    sheet.dismiss();
                    requestPermissions(new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 4105);
                }
            });
            list.addView(legacy);
        }
        if (list.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Nenhum download encontrado");
            empty.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
            empty.setPadding(0, dp(18), 0, dp(18));
            list.addView(empty);
        }
        sheet.setContentView(root);
        sheet.show();
        updateProgress[0].run();
    }

    private void addDownloadFileRow(final LinearLayout list, final Uri itemUri, String name, long size) {
        final TextView item = new TextView(this);
        item.setText((name == null ? "Arquivo" : name) + "\n"
            + android.text.format.Formatter.formatShortFileSize(this, size));
        item.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        item.setTextSize(14);
        item.setPadding(dp(8), dp(14), dp(8), dp(14));
        item.setBackgroundResource(android.R.drawable.list_selector_background);
        item.setClickable(true);
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent open = new Intent(Intent.ACTION_VIEW);
                open.setDataAndType(itemUri, getContentResolver().getType(itemUri));
                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try { startActivity(open); }
                catch (ActivityNotFoundException error) { Toast.makeText(MainActivity.this, "Nenhum aplicativo disponível para abrir este arquivo", Toast.LENGTH_LONG).show(); }
            }
        });
        item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Excluir download?")
                    .setMessage("O arquivo será removido da pasta Downloads")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            try {
                                int deleted = getContentResolver().delete(itemUri, null, null);
                                if (deleted > 0) {
                                    list.removeView(item);
                                    if (list.getChildCount() == 0) {
                                        TextView empty = new TextView(MainActivity.this);
                                        empty.setText("Nenhum download encontrado");
                                        empty.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
                                        empty.setPadding(0, dp(18), 0, dp(18));
                                        list.addView(empty);
                                    }
                                    Toast.makeText(MainActivity.this, "Download excluído", Toast.LENGTH_SHORT).show();
                                } else Toast.makeText(MainActivity.this, "Não foi possível excluir o arquivo", Toast.LENGTH_LONG).show();
                            } catch (SecurityException error) {
                                Toast.makeText(MainActivity.this, "O Android não autorizou a exclusão deste arquivo", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).show();
                return true;
            }
        });
        list.addView(item, new LinearLayout.LayoutParams(-1, -2));
    }

    private static final int REQUEST_IMPORT_BOOKMARKS = 6121;
    private static final int REQUEST_EXPORT_BOOKMARKS = 6122;
    private void importBookmarks() {
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("text/html");
        try { startActivityForResult(picker, REQUEST_IMPORT_BOOKMARKS); }
        catch (ActivityNotFoundException error) { Toast.makeText(this, "Não foi possível abrir o seletor de arquivos", Toast.LENGTH_LONG).show(); }
    }

    private void exportBookmarks() {
        Intent picker = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("text/html");
        picker.putExtra(Intent.EXTRA_TITLE, "snav-bookmarks.html");
        try { startActivityForResult(picker, REQUEST_EXPORT_BOOKMARKS); }
        catch (ActivityNotFoundException error) { Toast.makeText(this, "Não foi possível abrir o seletor de arquivos", Toast.LENGTH_LONG).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null || browserLibraryStore == null) return;
        Uri uri = data.getData();
        if (requestCode == ExtensionManager.REQUEST_XPI) {
            pendingExtensionFile = uri;
            return;
        }
        try {
            if (requestCode == REQUEST_IMPORT_BOOKMARKS) {
                StringBuilder html = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null && html.length() <= 2_000_000) html.append(line).append('\n');
                }
                int count = browserLibraryStore.importBookmarks(html.toString());
                Toast.makeText(this, count + " favoritos importados ou já existentes", Toast.LENGTH_LONG).show();
            } else if (requestCode == REQUEST_EXPORT_BOOKMARKS) {
                try (OutputStream output = getContentResolver().openOutputStream(uri)) {
                    if (output == null) throw new IOException("Destino indisponível");
                    output.write(browserLibraryStore.exportBookmarksHtml().getBytes("UTF-8"));
                    output.flush();
                }
                Toast.makeText(this, "Favoritos exportados", Toast.LENGTH_LONG).show();
            }
        } catch (Exception error) {
            logDiagnostic("Falha na importação/exportação de favoritos: " + error.getClass().getSimpleName());
            Toast.makeText(this, "Não foi possível processar o arquivo", Toast.LENGTH_LONG).show();
        }
    }

	private void showSiteInfoBottomSheet() {
		final String currentUrl = geckoEngine == null ? null : geckoEngine.getCurrentUri();
		final Uri pageUri = currentUrl == null ? null : Uri.parse(currentUrl);
		final String scheme = pageUri == null ? null : pageUri.getScheme();
		final String host = pageUri == null ? null : pageUri.getHost();
		final BottomSheetDialog sheet = new BottomSheetDialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(siteInfoDp(24), siteInfoDp(22), siteInfoDp(24), siteInfoDp(28));
		BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
		content.addView(handle, new LinearLayout.LayoutParams(-1, dp(48)));

		TextView title = new TextView(this);
		title.setText("Informações do site");
		title.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setPadding(0, 0, 0, siteInfoDp(18));
		content.addView(title);
		addSiteInfoRow(content, "Endereço completo", currentUrl == null ? "Nenhum endereço aberto" : currentUrl);

		addSiteInfoRow(content, "Conexão", "https".equalsIgnoreCase(scheme) ? "HTTPS — conexão criptografada" : "about".equalsIgnoreCase(scheme) ? "Página interna do GeckoView" : "Não disponível");
		addSiteInfoRow(content, "Proteção", "Proteção contra rastreamento e isolamento de cookies de terceiros pelo GeckoView");
		addSiteInfoRow(content, "Permissões", "Câmera, microfone e localização exigem autorização para o site e, quando necessário, para o aplicativo no Android");
        if (isSecureOrigin(currentUrl)) {
            addLibraryAction(content, "Gerenciar permissões do site", () -> {
                sheet.dismiss();
                showSitePermissions(currentUrl);
            });
        }
		addSiteInfoRow(content, "JavaScript", javaScriptEnabled ? "Ativado para o navegador" : "Desativado para o navegador");

		TextView clearData = new TextView(this);
		clearData.setText("Limpar dados deste site");
		clearData.setTextColor(themeColor(com.google.android.material.R.attr.colorOnPrimary, android.R.color.white));
		clearData.setTextSize(14);
		clearData.setGravity(Gravity.CENTER);
		clearData.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		clearData.setMinHeight(siteInfoDp(48));
		clearData.setPadding(siteInfoDp(16), siteInfoDp(10), siteInfoDp(16), siteInfoDp(10));
		clearData.setBackgroundResource(R.drawable.snav_accent_ripple);
		clearData.setClickable(true);
		clearData.setFocusable(true);
		LinearLayout.LayoutParams clearDataParams = new LinearLayout.LayoutParams(-1, dp(48));
		clearDataParams.topMargin = siteInfoDp(12);
		content.addView(clearData, clearDataParams);
		clearData.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (host == null || geckoEngine == null) {
					Toast.makeText(MainActivity.this, "Abra um site para limpar os dados", Toast.LENGTH_SHORT).show();
					return;
				}
				sheet.dismiss();
				showSnavDecisionSheet("Limpar dados do site?",
					"Remover cookies, armazenamento e permissões associados a " + host + "? A página atual será encerrada para evitar recriação imediata dos dados.",
					"Limpar", "Cancelar", new Runnable() {
						@Override public void run() {
							if (geckoEngine == null) return;
						geckoEngine.clearSiteData(host).accept(
							new GeckoResult.Consumer<Void>() {
								@Override public void accept(Void ignored) {
									if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Dados do site removidos", Toast.LENGTH_SHORT).show();
								}
							},
							new GeckoResult.Consumer<Throwable>() {
								@Override public void accept(Throwable error) {
									logDiagnostic("Falha ao limpar dados do site: " + error.getClass().getSimpleName());
									if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Não foi possível limpar os dados do site", Toast.LENGTH_LONG).show();
								}
							}
						);
						}
					}, null);
			}
		});
		TextView managePermissions = new TextView(this);
		managePermissions.setText("Gerenciar permissões do aplicativo");
		managePermissions.setTextColor(themeColor(com.google.android.material.R.attr.colorOnPrimary, android.R.color.white));
		managePermissions.setTextSize(14);
		managePermissions.setGravity(Gravity.CENTER);
		managePermissions.setMinHeight(siteInfoDp(48));
		managePermissions.setPadding(siteInfoDp(16), siteInfoDp(10), siteInfoDp(16), siteInfoDp(10));
		managePermissions.setBackgroundResource(R.drawable.snav_accent_ripple);
		managePermissions.setClickable(true);
		managePermissions.setFocusable(true);
		LinearLayout.LayoutParams managePermissionsParams = new LinearLayout.LayoutParams(-1, dp(48));
		managePermissionsParams.topMargin = siteInfoDp(10);
		content.addView(managePermissions, managePermissionsParams);
		managePermissions.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				sheet.dismiss();
				Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
				try { startActivity(settings); }
				catch (ActivityNotFoundException error) { Toast.makeText(MainActivity.this, "Não foi possível abrir as configurações do aplicativo", Toast.LENGTH_SHORT).show(); }
			}
		});
		ScrollView scroll = new ScrollView(this);
		scroll.setFillViewport(true);
		scroll.setClipToPadding(false);
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
		sheet.setContentView(scroll);
		sheet.setCancelable(true);
		sheet.show();
		sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
		sheet.getBehavior().setSkipCollapsed(true);
		if (sheet.getWindow() != null) sheet.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
	}



			@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 4103) {
			String[] requested = pendingAndroidPermissions;
			GeckoSession.PermissionDelegate.Callback callback = pendingAndroidPermissionCallback;
			String origin = pendingAndroidPermissionOrigin;
			pendingAndroidPermissionCallback = null;
			pendingAndroidPermissionOrigin = null;
			pendingAndroidPermissions = null;
			boolean permissionsGranted = areAndroidPermissionsGranted(requested);
			if (!permissionsGranted && Build.VERSION.SDK_INT >= 23 && requested != null) {
				boolean locationAvailable = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
					|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
				for (String permission : requested) {
					boolean locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
						|| android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission);
					if (locationPermission && locationAvailable) continue;
					if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED
						&& !shouldShowRequestPermissionRationale(permission)) {
						Dados.edit().putBoolean(permissionDenialKey(permission), true).apply();
					}
				}
			}
			boolean sameOrigin = geckoEngine != null && isSameOrigin(origin, geckoEngine.getCurrentUri());
			if (callback != null) {
				if (permissionsGranted && sameOrigin) callback.grant(); else callback.reject();
			}
			GeckoSession.PermissionDelegate.MediaCallback media = pendingMediaPermissionCallback;
			if (media != null) {
				String mediaOrigin = pendingMediaPermissionOrigin;
				GeckoSession.PermissionDelegate.MediaSource[] video = pendingVideoSources;
				GeckoSession.PermissionDelegate.MediaSource[] audio = pendingAudioSources;
				pendingMediaPermissionCallback = null;
				pendingMediaPermissionOrigin = null;
				pendingVideoSources = null;
				pendingAudioSources = null;
				if (permissionsGranted && geckoEngine != null && isSameOrigin(mediaOrigin, geckoEngine.getCurrentUri())) {
					showGeckoMediaPermissionPrompt(mediaOrigin, video, audio, media);
				} else {
					media.reject();
				}
			}
        } else if (requestCode == 4106) {
            pendingDownloadNotificationPermission = false;
        } else if (requestCode == 4107) {
            GeckoResult<Integer> result = pendingNotificationPermission;
            String origin = pendingNotificationOrigin;
            pendingNotificationPermission = null;
            pendingNotificationOrigin = null;
            if (result != null) finishContentPermission(result, grantResults != null && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
                && geckoEngine != null && isSameOrigin(origin, geckoEngine.getCurrentUri())
                && WebNotificationBridge.areEnabled(this));
		} else if (requestCode == 4105) {
            boolean granted = grantResults != null && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            if (granted) showDownloadsBottomSheet();
            else Toast.makeText(this, "Permita o acesso aos arquivos para ver os downloads", Toast.LENGTH_LONG).show();
        } else if (requestCode == 4104) {
			boolean granted = grantResults != null && grantResults.length > 0
				&& grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
			WebResponse response = pendingGeckoDownload;
			String name = pendingGeckoDownloadName;
			pendingGeckoDownload = null;
			pendingGeckoDownloadName = null;
			if (response != null) {
				if (granted) saveGeckoDownload(response, name);
				else {
					closeResponseBody(response);
					Toast.makeText(this, "Download cancelado. Permita o acesso aos arquivos para baixar.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

    private int themeColor(int attribute, int fallback) {
        return com.google.android.material.color.MaterialColors.getColor(this, attribute, getColor(fallback));
    }

    // Material Snackbar handles accessibility-adjusted timeouts and swipe dismissal.
    void showFeedback(String message, String action, Runnable onAction) {
        if (isFinishing() || isDestroyed()) return;
        View root = findViewById(R.id.snav_snackbar_host);
        if (root == null) return;
        com.google.android.material.snackbar.Snackbar snackbar =
            com.google.android.material.snackbar.Snackbar.make(root, message,
                action == null ? com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    : com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        if (action != null && onAction != null) snackbar.setAction(action, view -> onAction.run());
        snackbar.show();
    }

    private void addDynamicColorPreference() {
        if (linear33 == null || linear33.findViewWithTag("snav_dynamic_color") != null) return;
        LinearLayout row = new LinearLayout(this);
        row.setTag("snav_dynamic_color");
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        MaterialSwitch toggle = new MaterialSwitch(this);
        toggle.setText("Cores dinâmicas");
        toggle.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
        boolean available = com.google.android.material.color.DynamicColors.isDynamicColorAvailable();
        toggle.setChecked(available && Dados.getBoolean("dynamic_color", true));
        toggle.setEnabled(available);
        row.addView(toggle);
        TextView summary = new TextView(this);
        summary.setText(available ? "Usar as cores do dispositivo. Desative para usar as cores do Snav."
            : "Indisponível neste dispositivo. O Snav usa suas próprias cores.");
        summary.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
        summary.setTextSize(14);
        row.addView(summary);
        LinearLayout appearance = findViewById(R.id.tools_appearance);
        appearance.addView(row);
        toggle.setOnCheckedChangeListener((button, checked) -> {
            Dados.edit().putBoolean("dynamic_color", checked).apply();
            recreate();
        });
    }

		private void addTrackerBlockingPreference(boolean enabled) {
		if (linear33 == null || linear33.findViewWithTag("snav_tracker_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_tracker_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Proteção contra rastreamento");
		toggle.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Usa a proteção contra rastreamento do GeckoView");
		summary.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		summary.setTextSize(12);
		summary.setPadding(dp(40), 0, dp(8), 0);
		row.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
		row.addView(summary, new LinearLayout.LayoutParams(-1, -2));
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton button, boolean checked) {
				Dados.edit().putBoolean("tracker_blocking", checked).apply();
				if (geckoEngine != null) geckoEngine.setTrackingProtectionEnabled(checked);
				com.google.android.material.snackbar.Snackbar.make(linear33, checked ? "Proteção contra rastreamento ativada" : "Proteção contra rastreamento desativada", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
			}
		});
        LinearLayout privacy = findViewById(R.id.tools_privacy);
        int insertAt = Math.max(0, privacy.getChildCount() - 1);
        privacy.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
	}

	private void addDoNotTrackPreference(boolean enabled) {
		if (linear33 == null || linear33.findViewWithTag("snav_dnt_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_dnt_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Do Not Track");
		toggle.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Envia Do Not Track nas navegações iniciadas pelo Snav; os sites podem ignorar essa solicitação");
		summary.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		summary.setTextSize(12);
		summary.setPadding(dp(40), 0, dp(8), 0);
		row.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
		row.addView(summary, new LinearLayout.LayoutParams(-1, -2));
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton button, boolean checked) {
				Dados.edit().putBoolean("do_not_track", checked).apply();
				com.google.android.material.snackbar.Snackbar.make(linear33,
					checked ? "Do Not Track ativado" : "Do Not Track desativado",
					com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
			}
		});
        LinearLayout privacy = findViewById(R.id.tools_privacy);
        int insertAt = Math.max(0, privacy.getChildCount() - 1);
        privacy.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
	}

	private String domainForAddressBar(String uri) {
		if (uri == null || uri.trim().isEmpty()) return "Snav Browser";
		String candidate = uri;
		if (uri.regionMatches(true, 0, "about:reader?", 0, 13)) {
			if (readerOriginalUri != null) candidate = readerOriginalUri;
			else {
				int queryStart = uri.indexOf('?');
				if (queryStart >= 0) {
					for (String parameter : uri.substring(queryStart + 1).split("&")) {
						int separator = parameter.indexOf('=');
						if (separator > 0 && "url".equals(Uri.decode(parameter.substring(0, separator)))) {
							candidate = Uri.decode(parameter.substring(separator + 1).replace("+", "%20"));
							break;
						}
					}
				}
			}
		}
		try {
			String host = Uri.parse(candidate).getHost();
			return host == null || host.trim().isEmpty() ? "Snav Browser" : host;
		} catch (RuntimeException ignored) {
			return "Snav Browser";
		}
	}

	private void loadUrlWithPrivacyHeaders(String url) {
		if (geckoEngine == null || url == null) return;
        try {
            if (isSecureOrigin(url)) {
                vazio.setVisibility(View.GONE);
                if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
            }
            geckoEngine.loadUri(url, Dados.getBoolean("do_not_track", false));
        }
		catch (IllegalArgumentException | SecurityException error) {
			logDiagnostic("Navegação recusada: " + error.getClass().getSimpleName());
			Toast.makeText(this, "Endereço não permitido. O Snav exige HTTPS.", Toast.LENGTH_LONG).show();
		}
	}

					private void showSnavDecisionSheet(String titleText, String messageText,
		String positiveText, String negativeText, final Runnable positiveAction,
		final Runnable negativeAction) {
		final BottomSheetDialog sheet = new BottomSheetDialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(dp(24), dp(8), dp(24), dp(24));

		BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
		content.addView(handle, new LinearLayout.LayoutParams(-1, dp(40)));

		TextView title = new TextView(this);
		title.setText(titleText);
		title.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
		titleParams.bottomMargin = dp(12);
		content.addView(title, titleParams);

		View divider = new View(this);
		divider.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorOutlineVariant, R.color.snav_outline));
		content.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

		TextView message = new TextView(this);
		message.setText(messageText);
		message.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
		message.setTextSize(14);
		message.setLineSpacing(dp(3), 1f);
		LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(-1, -2);
		messageParams.topMargin = dp(16);
		messageParams.bottomMargin = dp(24);
		content.addView(message, messageParams);

		LinearLayout actions = new LinearLayout(this);
		actions.setOrientation(LinearLayout.HORIZONTAL);
		actions.setGravity(Gravity.CENTER_VERTICAL);
		MaterialButton negative = new MaterialButton(this);
		negative.setText(negativeText);
		negative.setAllCaps(false);
		negative.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		negative.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorSurface, R.color.snav_surface)));
		negative.setStrokeColor(android.content.res.ColorStateList.valueOf(themeColor(com.google.android.material.R.attr.colorOutlineVariant, R.color.snav_outline)));
		negative.setStrokeWidth(dp(1));
		negative.setCornerRadius(dp(20));
		MaterialButton positive = new MaterialButton(this);
		positive.setText(positiveText);
		positive.setAllCaps(false);
		positive.setTextColor(themeColor(com.google.android.material.R.attr.colorOnPrimary, android.R.color.white));
		positive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor(androidx.appcompat.R.attr.colorPrimary, R.color.snav_accent)));
		positive.setCornerRadius(dp(20));
		LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
		actions.addView(negative, buttonParams);
		LinearLayout.LayoutParams positiveParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
		positiveParams.leftMargin = dp(12);
		actions.addView(positive, positiveParams);
		content.addView(actions);

		sheet.setContentView(content);
		final boolean[] handled = new boolean[] { false };
		negative.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (handled[0]) return;
				handled[0] = true;
				sheet.dismiss();
				if (negativeAction != null) negativeAction.run();
			}
		});
		positive.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (handled[0]) return;
				handled[0] = true;
				sheet.dismiss();
				if (positiveAction != null) positiveAction.run();
			}
		});
		sheet.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override public void onCancel(DialogInterface dialog) {
				if (handled[0]) return;
				handled[0] = true;
				if (negativeAction != null) negativeAction.run();
			}
		});
		sheet.show();
		sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
		sheet.getBehavior().setSkipCollapsed(true);
	}

	private int dp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

		private void showInsecureConnectionDialog(final String url) {
		final Uri parsed = Uri.parse(url);
		if (parsed.getHost() == null || parsed.getUserInfo() != null) return;
		final String host = parsed.getHost();
		Uri.Builder secureBuilder = parsed.buildUpon().scheme("https");
		if (parsed.getUserInfo() != null || parsed.getHost() == null) return;
		if (parsed.getPort() == 80) {
			String authority = parsed.getEncodedAuthority();
			int portSeparator = authority == null ? -1 : authority.lastIndexOf(':');
			if (portSeparator > 0) secureBuilder.encodedAuthority(authority.substring(0, portSeparator));
		}
		final String secureUrl = secureBuilder.build().toString();
		showSnavDecisionSheet("HTTP bloqueado",
			"A conexão com " + host + " não é criptografada. O Snav permite apenas conexões HTTPS. Tentar abrir a versão segura?",
			"Tentar HTTPS", "Cancelar", new Runnable() {
				@Override public void run() { loadUrlWithPrivacyHeaders(secureUrl); showScreen(2); vazio.setVisibility(View.GONE); }
			}, null);
	}



	private boolean isSecureOrigin(String uri) {
		if (uri == null) return false;
		Uri parsed = Uri.parse(uri);
		return "https".equalsIgnoreCase(parsed.getScheme()) && parsed.getHost() != null && parsed.getUserInfo() == null;
	}

	private void installGeckoPermissionDelegate() {
		geckoEngine.getSession().setPermissionDelegate(new GeckoSession.PermissionDelegate() {
			@Override
			public void onAndroidPermissionsRequest(GeckoSession session, String[] permissions,
				GeckoSession.PermissionDelegate.Callback callback) {
				String origin = geckoEngine.getCurrentUri();
				if (!isSecureOrigin(origin)) {
					callback.reject();
					return;
				}
				if (permissions == null || permissions.length == 0) {
					callback.grant();
					return;
				}
				java.util.ArrayList<String> missing = new java.util.ArrayList<String>();
				for (String permission : permissions) {
                    if ((android.Manifest.permission.CAMERA.equals(permission) && !Dados.getBoolean("allow_camera", true))
                        || (android.Manifest.permission.RECORD_AUDIO.equals(permission) && !Dados.getBoolean("allow_microphone", true))
                        || ((android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) || android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission))
                            && !Dados.getBoolean("allow_location", true))) { callback.reject(); return; }
					if (!android.Manifest.permission.CAMERA.equals(permission)
						&& !android.Manifest.permission.RECORD_AUDIO.equals(permission)
						&& !android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
						&& !android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
						callback.reject();
						return;
					}
					if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) continue;
					if (android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
						&& checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) continue;
					if (android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)
						&& checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) continue;
					if (isPermissionPermanentlyDenied(permission)) {
						callback.reject();
						return;
					}
					missing.add(permission);
				}
				if (missing.isEmpty() || Build.VERSION.SDK_INT < 23) {
					callback.grant();
					return;
				}
				if (pendingAndroidPermissionCallback != null) pendingAndroidPermissionCallback.reject();
				pendingAndroidPermissionCallback = callback;
				pendingAndroidPermissionOrigin = origin;
				pendingAndroidPermissions = missing.toArray(new String[missing.size()]);
				requestPermissions(pendingAndroidPermissions, 4103);
			}

			@Override
			public GeckoResult<Integer> onContentPermissionRequest(GeckoSession session,
				final GeckoSession.PermissionDelegate.ContentPermission permission) {
                String label = requestableContentPermissionLabel(permission.permission);
                final String topOrigin = geckoEngine == null ? null : geckoEngine.getCurrentUri();
                if (label == null || !isSecureOrigin(permission.uri) || !isSecureOrigin(topOrigin)) {
                    return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
                }
                if (permission.thirdPartyOrigin != null && !isSecureOrigin(permission.thirdPartyOrigin)) {
                    return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
                }
                if (!isContentPermissionEnabled(permission.permission)) {
                    return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
                }
                // Muted autoplay is non-sensitive and occurs frequently; never prompt for it.
                if (permission.permission == GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE) {
                    return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW);
                }
                final GeckoResult<Integer> result = new GeckoResult<>();
                pendingContentPermissions.add(result);
                permission.notifyShown();
                String requester = Uri.parse(permission.uri).getHost();
                String thirdParty = permission.thirdPartyOrigin == null ? ""
                    : " Terceiro envolvido: " + Uri.parse(permission.thirdPartyOrigin).getHost() + ".";
                String warning = permission.permission == GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE
                    ? " Após concedida, essa permissão não pode ser revogada individualmente pelo GeckoView." : "";
                showSnavDecisionSheet("Permitir " + label + "?",
                    "O site " + requester + " solicita " + label + "." + thirdParty + warning,
                    "Permitir", "Bloquear", () -> {
                        if (geckoEngine == null || !isSameOrigin(topOrigin, geckoEngine.getCurrentUri())) {
                            finishContentPermission(result, false);
                        } else if (permission.permission == GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION) {
                            requestSiteNotificationPermission(result, topOrigin);
                        } else finishContentPermission(result, true);
                    },
                    () -> finishContentPermission(result, false));
                return result;
			}

			@Override
			public void onMediaPermissionRequest(GeckoSession session, String uri,
				GeckoSession.PermissionDelegate.MediaSource[] video,
				GeckoSession.PermissionDelegate.MediaSource[] audio,
				GeckoSession.PermissionDelegate.MediaCallback callback) {
				boolean hasVideo = video != null && video.length > 0;
				boolean hasAudio = audio != null && audio.length > 0;
				if (!isSecureOrigin(uri) || (!hasVideo && !hasAudio)
                    || (hasVideo && !Dados.getBoolean("allow_camera", true))
                    || (hasAudio && !Dados.getBoolean("allow_microphone", true))) {
					callback.reject();
					return;
				}
				if (pendingAndroidPermissionCallback != null) {
					pendingMediaPermissionCallback = callback;
					pendingVideoSources = video;
					pendingAudioSources = audio;
					pendingMediaPermissionOrigin = uri;
					return;
				}
				showGeckoMediaPermissionPrompt(uri, video, audio, callback);
			}
		});
	}

    private void requestSiteNotificationPermission(GeckoResult<Integer> result, String origin) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (pendingNotificationPermission != null || pendingAndroidPermissionCallback != null || pendingDownloadNotificationPermission) {
                finishContentPermission(result, false); return;
            }
            pendingNotificationPermission = result;
            pendingNotificationOrigin = origin;
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 4107);
        } else {
            finishContentPermission(result, WebNotificationBridge.areEnabled(this));
        }
    }

    private void finishContentPermission(GeckoResult<Integer> result, boolean allow) {
        if (!pendingContentPermissions.remove(result)) return;
        result.complete(allow ? GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
            : GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
    }

    static String requestableContentPermissionLabel(int type) {
        switch (type) {
            case GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION: return "localização";
            case GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION: return "notificações";
            case GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE: return "armazenamento persistente";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE: return "reprodução automática sem som";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE: return "reprodução automática com som";
            case GeckoSession.PermissionDelegate.PERMISSION_STORAGE_ACCESS: return "acesso a cookies e armazenamento";
            case GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS: return "mídia protegida";
            default: return null; // Do not grant capabilities without a functioning host integration.
        }
    }

    static String contentPermissionPreference(int type) {
        switch (type) {
            case GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION: return "allow_location";
            case GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION: return "allow_site_notifications";
            case GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE: return "allow_persistent_storage";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE: return "allow_autoplay_muted";
            case GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE: return "allow_autoplay_sound";
            case GeckoSession.PermissionDelegate.PERMISSION_STORAGE_ACCESS: return "allow_site_storage";
            case GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS: return "allow_protected_media";
            default: return null;
        }
    }

    private boolean isContentPermissionEnabled(int type) {
        String key = contentPermissionPreference(type);
        return key != null && Dados.getBoolean(key, true);
    }

    private void addSitePermissionPreferences() {
        LinearLayout privacy = findViewById(R.id.tools_privacy);
        if (privacy.findViewWithTag("snav_permissions") != null) return;
        LinearLayout group = new LinearLayout(this);
        group.setTag("snav_permissions");
        group.setOrientation(LinearLayout.VERTICAL);
        TextView heading = new TextView(this);
        heading.setText("Permissões de sites");
        heading.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
        heading.setPadding(dp(8), dp(16), dp(8), dp(4));
        group.addView(heading);
        TextView explanation = new TextView(this);
        explanation.setText("Controla novas solicitações. Decisões já armazenadas pelo GeckoView e capturas em andamento podem continuar; revise permissões nas informações do site.");
        explanation.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.snav_on_surface_variant));
        explanation.setPadding(dp(8), 0, dp(8), dp(8));
        group.addView(explanation);
        String[][] settings = {
            {"Localização", "allow_location"}, {"Câmera", "allow_camera"},
            {"Microfone", "allow_microphone"}, {"Notificações de sites", "allow_site_notifications"},
            {"Reprodução automática sem som", "allow_autoplay_muted"},
            {"Reprodução automática com som", "allow_autoplay_sound"},
            {"Armazenamento persistente", "allow_persistent_storage"},
            {"Acesso a cookies e armazenamento", "allow_site_storage"},
            {"Mídia protegida", "allow_protected_media"}
        };
        for (String[] setting : settings) {
            MaterialSwitch toggle = new MaterialSwitch(this);
            toggle.setText(setting[0]);
            toggle.setMinHeight(dp(48));
            toggle.setPadding(dp(8), 0, dp(8), 0);
            toggle.setChecked(Dados.getBoolean(setting[1], true));
            toggle.setOnCheckedChangeListener((button, checked) -> Dados.edit().putBoolean(setting[1], checked).apply());
            group.addView(toggle, new LinearLayout.LayoutParams(-1, -2));
        }
        privacy.addView(group);
    }

    private static String storedPermissionLabel(int type) {
        String label = requestableContentPermissionLabel(type);
        if (label != null) return label;
        switch (type) {
            case GeckoSession.PermissionDelegate.PERMISSION_XR: return "realidade virtual";
            case GeckoSession.PermissionDelegate.PERMISSION_TRACKING: return "rastreamento";
            case GeckoSession.PermissionDelegate.PERMISSION_LOCAL_DEVICE_ACCESS: return "acesso ao dispositivo local";
            case GeckoSession.PermissionDelegate.PERMISSION_LOCAL_NETWORK_ACCESS: return "acesso à rede local";
            default: return "Permissão " + type;
        }
    }

    private void showSitePermissions(String uri) {
        if (geckoEngine == null || !isSecureOrigin(uri)) return;
        geckoEngine.getSitePermissions(uri).accept(permissions -> {
            if (isFinishing() || isDestroyed() || geckoEngine == null || !isSameOrigin(uri, geckoEngine.getCurrentUri())) return;
            if (permissions == null || permissions.isEmpty()) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Permissões do site")
                    .setMessage("Nenhuma permissão de conteúdo armazenada para este site. Câmera e microfone também dependem das permissões do Android.")
                    .setPositiveButton("Fechar", null).show();
                return;
            }
            String[] labels = new String[permissions.size()];
            for (int i = 0; i < labels.length; i++) {
                GeckoSession.PermissionDelegate.ContentPermission p = permissions.get(i);
                String value = p.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW ? "Permitido"
                    : p.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY ? "Bloqueado" : "Perguntar";
                labels[i] = storedPermissionLabel(p.permission) + " · " + value
                    + (p.thirdPartyOrigin == null ? "" : " · " + p.thirdPartyOrigin);
            }
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Permissões do site")
                .setItems(labels, (dialog, index) -> {
                    GeckoSession.PermissionDelegate.ContentPermission p = permissions.get(index);
                    if (p.permission == GeckoSession.PermissionDelegate.PERMISSION_PERSISTENT_STORAGE
                        && p.value == GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW) {
                        showFeedback("O GeckoView não permite revogar esta permissão individualmente", null, null);
                        return;
                    }
                    String[] actions = p.permission == GeckoSession.PermissionDelegate.PERMISSION_TRACKING
                        ? new String[]{"Bloquear"} : new String[]{"Bloquear", "Perguntar novamente"};
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle(storedPermissionLabel(p.permission)).setItems(actions, (d, action) -> {
                            if (geckoEngine == null || !isSameOrigin(uri, geckoEngine.getCurrentUri())) return;
                            geckoEngine.setSitePermission(p, action == 0
                                ? GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
                                : GeckoSession.PermissionDelegate.ContentPermission.VALUE_PROMPT);
                            showFeedback("Preferência enviada ao GeckoView", null, null);
                        }).setNegativeButton("Cancelar", null).show();
                }).setNegativeButton("Fechar", null).show();
        }, error -> { if (!isFinishing() && !isDestroyed()) showFeedback("Não foi possível consultar as permissões do site", null, null); });
    }

	private String permissionDenialKey(String permission) {
		return "gecko_permission_permanently_denied_" + permission.replace('.', '_');
	}

	private boolean isPermissionPermanentlyDenied(String permission) {
		if (Build.VERSION.SDK_INT < 23 || Dados == null
			|| checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) return false;
		if (android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
			|| android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
			if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) return false;
		}
		return Dados.getBoolean(permissionDenialKey(permission), false);
	}

	private void clearGrantedPermissionDenials() {
		if (Build.VERSION.SDK_INT < 23 || Dados == null) return;
		String[] permissions = new String[] {
			android.Manifest.permission.CAMERA,
			android.Manifest.permission.RECORD_AUDIO,
			android.Manifest.permission.ACCESS_FINE_LOCATION,
			android.Manifest.permission.ACCESS_COARSE_LOCATION
		};
		for (String permission : permissions) {
			if (checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				Dados.edit().remove(permissionDenialKey(permission)).apply();
			}
		}
	}

	private boolean areAndroidPermissionsGranted(String[] permissions) {
		if (Build.VERSION.SDK_INT < 23 || permissions == null || permissions.length == 0) return true;
		boolean locationRequested = false;
		for (String permission : permissions) {
			if (android.Manifest.permission.CAMERA.equals(permission)
				|| android.Manifest.permission.RECORD_AUDIO.equals(permission)) {
				if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) return false;
			} else if (android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
				|| android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
				locationRequested = true;
			} else {
				return false;
			}
		}
		return !locationRequested
			|| checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
			|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
	}

	private boolean isSameOrigin(String first, String second) {
		if (!isSecureOrigin(first) || !isSecureOrigin(second)) return false;
		Uri a = Uri.parse(first);
		Uri b = Uri.parse(second);
		int firstPort = a.getPort() < 0 ? 443 : a.getPort();
		int secondPort = b.getPort() < 0 ? 443 : b.getPort();
		return a.getHost().equalsIgnoreCase(b.getHost()) && firstPort == secondPort;
	}

    private static GeckoSession.PermissionDelegate.MediaSource firstMediaSource(
        GeckoSession.PermissionDelegate.MediaSource[] sources, int expected) {
        if (sources != null) for (GeckoSession.PermissionDelegate.MediaSource source : sources)
            if (source != null && source.source == expected) return source;
        return null;
    }

	private void showGeckoMediaPermissionPrompt(String uri,
		GeckoSession.PermissionDelegate.MediaSource[] video,
		GeckoSession.PermissionDelegate.MediaSource[] audio,
		final GeckoSession.PermissionDelegate.MediaCallback callback) {
        final GeckoSession.PermissionDelegate.MediaSource camera = firstMediaSource(video, GeckoSession.PermissionDelegate.MediaSource.SOURCE_CAMERA);
        final GeckoSession.PermissionDelegate.MediaSource microphone = firstMediaSource(audio, GeckoSession.PermissionDelegate.MediaSource.SOURCE_MICROPHONE);
        boolean hasVideo = video != null && video.length > 0;
        boolean hasAudio = audio != null && audio.length > 0;
        if ((hasVideo && camera == null) || (hasAudio && microphone == null)) { callback.reject(); return; }
		if (!hasVideo && !hasAudio) {
			callback.reject();
			return;
		}
		if (hasVideo && Build.VERSION.SDK_INT >= 23
			&& checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
			callback.reject();
			return;
		}
		if (hasAudio && Build.VERSION.SDK_INT >= 23
			&& checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
			callback.reject();
			return;
		}
		String host = Uri.parse(uri).getHost();
		showSnavDecisionSheet(hasVideo && hasAudio ? "Permitir câmera e microfone?" : hasVideo ? "Permitir câmera?" : "Permitir microfone?",
            host + (hasVideo && hasAudio ? " solicita acesso à câmera e ao microfone" : hasVideo ? " solicita acesso à câmera" : " solicita acesso ao microfone")
                + ". Permita apenas se você iniciou uma chamada ou gravação.",
			"Permitir", "Bloquear", new Runnable() {
				@Override public void run() {
                    if (geckoEngine != null && isSameOrigin(uri, geckoEngine.getCurrentUri())) callback.grant(camera, microphone);
                    else callback.reject();
                }
			}, new Runnable() {
				@Override public void run() { callback.reject(); }
			});
	}

	private void openExternalUri(String value) {
		if (value == null) return;
		Uri uri = Uri.parse(value);
		String scheme = uri.getScheme();
		if (scheme == null || uri.getUserInfo() != null
			|| "file".equalsIgnoreCase(scheme) || "content".equalsIgnoreCase(scheme)
			|| "javascript".equalsIgnoreCase(scheme) || "data".equalsIgnoreCase(scheme)) {
			return;
		}
		if (!"mailto".equalsIgnoreCase(scheme) && !"tel".equalsIgnoreCase(scheme)
			&& !"sms".equalsIgnoreCase(scheme) && !"geo".equalsIgnoreCase(scheme)) return;
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		} catch (ActivityNotFoundException error) {
			Toast.makeText(this, "Nenhum aplicativo disponível para abrir este endereço", Toast.LENGTH_SHORT).show();
		}
	}

		private void clearGeckoBrowsingData() {
		if (geckoEngine == null) return;
		geckoEngine.clearBrowsingData().accept(
			new GeckoResult.Consumer<Void>() {
				@Override public void accept(Void ignored) {
                    browserLibraryStore.clearHistory();
                    refreshHomeShortcuts();
                    updateTopBar();
					if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Dados de navegação removidos", Toast.LENGTH_SHORT).show();
				}
			},
			new GeckoResult.Consumer<Throwable>() {
				@Override public void accept(Throwable error) {
					logDiagnostic("Falha ao limpar dados: " + error.getClass().getSimpleName());
					if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Não foi possível limpar os dados", Toast.LENGTH_LONG).show();
				}
			});
	}

	private String responseHeader(WebResponse response, String name) {
		if (response == null || response.headers == null) return null;
		for (java.util.Map.Entry<String, String> entry : response.headers.entrySet()) {
			if (name.equalsIgnoreCase(entry.getKey())) return entry.getValue();
		}
		return null;
	}

	private void closeResponseBody(WebResponse response) {
		if (response != null && response.body != null) {
			try { response.body.close(); } catch (IOException ignored) {}
		}
	}

	private String guessDownloadFileName(String uri, String contentDisposition) {
		String name = null;
		if (contentDisposition != null) {
			java.util.regex.Matcher encoded = java.util.regex.Pattern.compile("(?i)filename\\*\\s*=\\s*UTF-8''([^;]+)").matcher(contentDisposition);
			if (encoded.find()) {
				try { name = java.net.URLDecoder.decode(encoded.group(1).trim(), "UTF-8"); }
				catch (Exception ignored) {}
			}
			if (name == null) {
				java.util.regex.Matcher plain = java.util.regex.Pattern.compile("(?i)filename\\s*=\\s*\"?([^;\"]+)\"?").matcher(contentDisposition);
				if (plain.find()) name = plain.group(1).trim();
			}
		}
		if (name == null || name.trim().isEmpty()) {
			String segment = Uri.parse(uri).getLastPathSegment();
			name = segment == null || segment.trim().isEmpty() ? "download" : segment;
		}
		name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
		return name.isEmpty() ? "download" : name;
	}

	private void handleGeckoDownload(final WebResponse response) {
		if (response == null || response.body == null || response.uri == null || !response.isSecure
			|| response.statusCode < 200 || response.statusCode >= 300) {
			closeResponseBody(response);
			Toast.makeText(this, "Download bloqueado: resposta inválida ou conexão não segura", Toast.LENGTH_LONG).show();
			return;
		}
		Uri uri = Uri.parse(response.uri);
		if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
			closeResponseBody(response);
			Toast.makeText(this, "Download bloqueado: somente HTTPS é permitido", Toast.LENGTH_LONG).show();
			return;
		}
		String disposition = responseHeader(response, "Content-Disposition");
		final String name = guessDownloadFileName(response.uri, disposition);
		response.setReadTimeoutMillis(120000);
		showSnavDecisionSheet("Salvar download?",
			"Salvar " + name + " na pasta Downloads",
			"Baixar", "Cancelar", new Runnable() {
				@Override public void run() {
					if (Build.VERSION.SDK_INT < 29 && Build.VERSION.SDK_INT >= 23
						&& checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
						pendingGeckoDownload = response;
						pendingGeckoDownloadName = name;
						requestPermissions(new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 4104);
					} else {
						saveGeckoDownload(response, name);
					}
				}
			}, new Runnable() {
				@Override public void run() { closeResponseBody(response); }
			});
	}

	private java.io.File createUniqueDownloadFile(java.io.File directory, String requestedName) throws IOException {
		java.io.File candidate = new java.io.File(directory, requestedName);
		if (!candidate.exists()) return candidate;
		String base = requestedName;
		String extension = "";
		int dot = requestedName.lastIndexOf('.');
		if (dot > 0) {
			base = requestedName.substring(0, dot);
			extension = requestedName.substring(dot);
		}
		for (int index = 1; index < 10000; index++) {
			candidate = new java.io.File(directory, base + " (" + index + ")" + extension);
			if (!candidate.exists()) return candidate;
		}
		throw new IOException("Não foi possível gerar um nome exclusivo para o download");
	}

    private void updateDownloadNotification(int id, DownloadStatus status, boolean finished, boolean failed) {
        android.app.NotificationManager manager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(new android.app.NotificationChannel(DOWNLOAD_CHANNEL, "Downloads", android.app.NotificationManager.IMPORTANCE_LOW));
        }
        Intent open = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("snav_show_downloads", true);
        android.app.PendingIntent pending = android.app.PendingIntent.getActivity(this, id, open,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, DOWNLOAD_CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(failed ? "Falha no download" : finished ? "Download concluído" : "Baixando")
            .setContentText(status.name).setOnlyAlertOnce(true).setOngoing(!finished && !failed)
            .setAutoCancel(finished || failed).setContentIntent(pending);
        if (!finished && !failed) {
            int percent = status.total > 0 ? Math.min(100, (int) (status.bytes * 100.0 / status.total)) : 0;
            builder.setProgress(100, percent, status.total <= 0);
        }
        try { manager.notify(id, builder.build()); } catch (SecurityException ignored) { }
    }

	private void saveGeckoDownload(final WebResponse response, final String fileName) {
		if (downloadExecutor == null || downloadExecutor.isShutdown()) {
			closeResponseBody(response);
			return;
		}
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
            && !Dados.getBoolean("download_notification_asked", false)
            && pendingNotificationPermission == null && pendingAndroidPermissionCallback == null) {
            Dados.edit().putBoolean("download_notification_asked", true).apply();
            pendingDownloadNotificationPermission = true;
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 4106);
        }
        long length = -1;
        try { length = Long.parseLong(responseHeader(response, "Content-Length")); } catch (Exception ignored) { }
        final DownloadStatus status = new DownloadStatus(fileName, length);
        final int notificationId = DOWNLOAD_IDS.incrementAndGet();
        ACTIVE_DOWNLOADS.put(notificationId, status);
        updateDownloadNotification(notificationId, status, false, false);
		try {
			downloadExecutor.execute(new Runnable() {
			@Override public void run() {
				Uri mediaUri = null;
				java.io.File legacyFile = null;
				try {
					java.io.OutputStream output;
					if (Build.VERSION.SDK_INT >= 29) {
						android.content.ContentValues values = new android.content.ContentValues();
						values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
						String mime = responseHeader(response, "Content-Type");
						values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime == null ? "application/octet-stream" : mime.split(";")[0].trim());
						values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
						values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1);
						mediaUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
						if (mediaUri == null) throw new IOException("Não foi possível criar o arquivo de download");
						output = getContentResolver().openOutputStream(mediaUri);
					} else {
						java.io.File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
						if (!directory.exists() && !directory.mkdirs()) throw new IOException("Não foi possível criar a pasta Downloads");
						legacyFile = createUniqueDownloadFile(directory, fileName);
						output = new java.io.FileOutputStream(legacyFile);
					}
					if (output == null) throw new IOException("Destino de download indisponível");
					try (java.io.InputStream input = response.body; java.io.OutputStream stream = output) {
						byte[] buffer = new byte[32768];
						int read;
                        long lastUpdate = 0;
						while ((read = input.read(buffer)) != -1) {
                            stream.write(buffer, 0, read);
                            status.bytes += read;
                            long now = android.os.SystemClock.elapsedRealtime();
                            if (now - lastUpdate >= 500) {
                                updateDownloadNotification(notificationId, status, false, false);
                                lastUpdate = now;
                            }
                        }
						stream.flush();
					}
					if (Build.VERSION.SDK_INT >= 29 && mediaUri != null) {
						android.content.ContentValues ready = new android.content.ContentValues();
						ready.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0);
						getContentResolver().update(mediaUri, ready, null, null);
					} else if (legacyFile != null) {
						android.media.MediaScannerConnection.scanFile(getApplicationContext(),
							new String[] { legacyFile.getAbsolutePath() }, null, null);
					}
                    ACTIVE_DOWNLOADS.remove(notificationId);
                    updateDownloadNotification(notificationId, status, true, false);
					runOnUiThread(new Runnable() {
						@Override public void run() {
							if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Download salvo na pasta Downloads", Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception error) {
                    ACTIVE_DOWNLOADS.remove(notificationId);
                    updateDownloadNotification(notificationId, status, false, true);
					if (mediaUri != null) getContentResolver().delete(mediaUri, null, null);
					if (legacyFile != null && legacyFile.exists()) legacyFile.delete();
					closeResponseBody(response);
					logDiagnostic("Falha ao salvar download Gecko: " + error.getClass().getSimpleName());
					runOnUiThread(new Runnable() {
						@Override public void run() {
							if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Não foi possível salvar o download", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
			});
		} catch (java.util.concurrent.RejectedExecutionException error) {
            ACTIVE_DOWNLOADS.remove(notificationId);
            updateDownloadNotification(notificationId, status, false, true);
			closeResponseBody(response);
		}
	}
	private void setupUnsplashWallpaper() {
		if (checkbox_wallpaper == null) return;
		if (!checkbox_wallpaper.isChecked()) {
			tela1.setBackgroundColor(themeColor(android.R.attr.colorBackground, R.color.snav_background));
			text_icon.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
			return;
		}
		text_icon.setTextColor(themeColor(com.google.android.material.R.attr.colorOnSurface, R.color.snav_on_surface));
		if (wallpaperManager == null) wallpaperManager = new UnsplashWallpaperManager(this);
		wallpaperCallback = new UnsplashWallpaperManager.Callback() {
			@Override
			public void onWallpaperReady(Bitmap bitmap, UnsplashWallpaperManager.Photo photo) {
                TextView credit = findViewById(R.id.wallpaper_credit);
				if (isFinishing() || isDestroyed() || checkbox_wallpaper == null || !checkbox_wallpaper.isChecked()
                    || credit == null || tela1 == null || !tela1.isAttachedToWindow()) {
					if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
					return;
				}
				tela1.setBackground(new UnsplashWallpaperDrawable(bitmap));
				text_icon.setTextColor(Color.WHITE);
                credit.setText("Foto: " + photo.author + " / Unsplash");
                credit.setContentDescription("Abrir foto de " + photo.author + " no Unsplash");
                credit.setOnClickListener(view -> {
                    loadUrlWithPrivacyHeaders(photo.pageUrl);
                    showScreen(2);
                    vazio.setVisibility(View.GONE);
                });
                credit.setVisibility(View.VISIBLE);
			}
		};
		wallpaperManager.load(true, wallpaperCallback);
	}

	private static final class UnsplashWallpaperDrawable extends android.graphics.drawable.Drawable {
		private final Bitmap bitmap;
		private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		UnsplashWallpaperDrawable(Bitmap image) {
			bitmap = image;
			shadePaint.setColor(0x66000000);
		}
		@Override public void draw(Canvas canvas) {
			if (bitmap == null || bitmap.isRecycled()) return;
			android.graphics.Rect bounds = getBounds();
			if (bounds.isEmpty()) return;
			float scale = Math.max((float) bounds.width() / bitmap.getWidth(),
				(float) bounds.height() / bitmap.getHeight());
			float width = bitmap.getWidth() * scale;
			float height = bitmap.getHeight() * scale;
			float left = bounds.left + (bounds.width() - width) / 2f;
			float top = bounds.top + (bounds.height() - height) / 2f;
			int saveCount = canvas.save();
			canvas.clipRect(bounds);
			canvas.drawBitmap(bitmap, null, new android.graphics.RectF(left, top, left + width, top + height), imagePaint);
			canvas.drawRect(bounds, shadePaint);
			canvas.restoreToCount(saveCount);
		}
		@Override public void setAlpha(int alpha) {
			imagePaint.setAlpha(alpha);
			shadePaint.setAlpha(Math.round(alpha * (102f / 255f)));
			invalidateSelf();
		}
		@Override public void setColorFilter(android.graphics.ColorFilter filter) {
			imagePaint.setColorFilter(filter);
			invalidateSelf();
		}
		@Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
	}

    private void setupPullToRefresh() {
        if (!(geckoView instanceof RefreshAwareGeckoView) || browserContentContainer == null) return;
        final RefreshAwareGeckoView browser = (RefreshAwareGeckoView) geckoView;
        browser.setEligiblePage(() -> geckoScrollY <= 0 && !readerMode && hasPageForActions()
            ? geckoEngine.getCurrentUri() : null);
        refreshHost = new RefreshComposeHost(this, browserContentContainer, browser, new Runnable() {
            @Override public void run() {
                String confirmedPage = browser.consumeConfirmedRefresh();
                if (confirmedPage == null || !hasPageForActions()
                    || !confirmedPage.equals(geckoEngine.getCurrentUri())) return;
                beginPageProgress(); // block duplicate reloads before Gecko's start callback
                refreshHost.setRefreshing(true);
                geckoEngine.reload();
            }
        });
    }



	private void initializeLogic() {
		_armazenamento();
		_open_sans();
		_interfaces();
		_internet();
		_gone();
	}
	
	@Override
	public void onBackPressed() {
        if (activePanel != null) { activePanel.dismiss(); return; }
        if (tela3.getVisibility() == View.VISIBLE) {
            returnFromTools();
            return;
        }
		if (readerMode) {
			exitReaderMode();
			return;
		}
		if (readerModePending) {
			readerRequestId++;
			readerModePending = false;
			readerOriginalUri = null;
			return;
		}
		if (geckoEngine == null) {
			handleBackNavigationFallback();
			return;
		}
		geckoEngine.processBackPressed().accept(
			new GeckoResult.Consumer<Boolean>() {
				@Override public void accept(Boolean handled) {
					if (!Boolean.TRUE.equals(handled)) handleBackNavigationFallback();
				}
			},
			new GeckoResult.Consumer<Throwable>() {
				@Override public void accept(Throwable error) { handleBackNavigationFallback(); }
			});
	}

	private void handleBackNavigationFallback() {
		if (geckoEngine != null && geckoEngine.canGoBack()) {
			geckoEngine.goBack();
			return;
		}
        showScreen(1);
		toque++;
		if (toque == 1) {
			time = new TimerTask() {
				@Override public void run() {
					runOnUiThread(new Runnable() {
						@Override public void run() { toque = 0; }
					});
				}
			};
			_timer.schedule(time, 2000);
		} else {
			finish();
		}
	}
	public void _gone() {
        showScreen(1);
	}
	
	
	public void _open_sans() {
		//fonte
		text_barra.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		edittext1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text_icon.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_sans_bold.ttf"), 0);
		//text
		text1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text5.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);


		//checkbox
		checkbox_javascript2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		checkbox_brilho.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
	}
	
	
	public void _interfaces() {
		// A barra mantém o RippleDrawable definido no XML.
		linear_barra_pesquisa.setElevation(0);
		
		// vscroll1
		vscroll1.setVerticalScrollBarEnabled(false); vscroll1.setHorizontalScrollBarEnabled(false);
	}
	
	
	public void _internet() {
        prog.setVisibility(View.GONE);
        prog.setAlpha(1f);
        prog.setProgress(0);
        prog.setIndeterminate(true);
        addTrackerBlockingPreference(Dados.getBoolean("tracker_blocking", true));
        addDoNotTrackPreference(Dados.getBoolean("do_not_track", false));
        addDynamicColorPreference();
        addSitePermissionPreferences();
        setupToolDestinations();
        javaScriptEnabled = !"desativado".equals(Dados.getString("java", "ativado"));
        restorePersistedBrowserTabs();
        geckoView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        geckoEngine = new GeckoEngineController(this, geckoView,
            new GeckoEngineController.Listener() {
                @Override public void onPageStarted(String uri) {
                    logDiagnostic("Navegação Gecko iniciada: " + safeHostForDiagnostics(uri));
                    loadingPageUrl = uri;
                    beginPageProgress();
                    vazio.setVisibility(View.GONE);
                    stoppedByUser = false;
                    failedPageUrl = null;
                    if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
                    updateTopBar();
                }
                @Override public void onPageStopped(boolean success) {
                    loadingPageUrl = null;
                    finishPageProgress();
                    stoppedByUser = false;
                    updateTopBar();
                    // A stop can belong to an interrupted load; it must not decide
                    // whether the current tab is empty or replace its content.
                }
                @Override public void onPageLoadError(String uri) {
                    if (stoppedByUser || !isSecureOrigin(uri)) return;
                    logDiagnostic("Falha de carregamento Gecko: " + safeHostForDiagnostics(uri));
                    failedPageUrl = uri;
                    vazio.setVisibility(View.GONE);
                    if (pageErrorView != null) pageErrorView.setVisibility(View.VISIBLE);
                    updateTopBar();
                }
                @Override public void onProgressChanged(int value) {
                    if (!pageLoading) beginPageProgress();
                    if (value > 0) {
                        prog.setIndeterminate(false);
                        if (progressAnimator != null) progressAnimator.cancel();
                        progressAnimator = ObjectAnimator.ofInt(prog, "progress", prog.getProgress(), Math.min(100, value));
                        progressAnimator.setDuration(100);
                        progressAnimator.start();
                    }
                }
                @Override public void onLocationChanged(String uri) {
                    if (extensionManager != null) extensionManager.onPageChanged();
                    if (uri != null && readerOriginalUri != null && !uri.equals(readerOriginalUri)) {
                        if (readerModePending) {
                            readerRequestId++;
                            readerModePending = false;
                            readerOriginalUri = null;
                        } else if (readerMode) {
                            exitReaderMode();
                        }
                    }
                    text_barra.setText(domainForAddressBar(uri));
                    updateTopBar();
                    if (!browserTabs.isEmpty()) { browserTabs.get(activeBrowserTab).url = uri; persistBrowserTabs(); }
                    if (browserLibraryStore != null && uri != null && uri.startsWith("https://")) {
                        currentPageTitle = null;
                        browserLibraryStore.addVisit(null, uri);
                        refreshHomeShortcuts();
                    }
                }
                @Override public void onTitleChanged(String title) {
                    currentPageTitle = title;
                    if (!browserTabs.isEmpty()) { browserTabs.get(activeBrowserTab).title = title; persistBrowserTabs(); }
                    String activeUri = geckoEngine == null ? null : geckoEngine.getCurrentUri();
                    if (browserLibraryStore != null && activeUri != null && activeUri.startsWith("https://")) {
                        browserLibraryStore.addVisit(title, activeUri);
                        refreshHomeShortcuts();
                    }
                    // The address bar intentionally displays the domain, never the page title.
                }
                @Override public void onInsecureNavigationBlocked(String uri, String source) {
                    showInsecureConnectionDialog(uri);
                }
                @Override public void onExternalNavigationRequested(String uri) {
                    openExternalUri(uri);
                }
                @Override public void onExtensionInstallRequested(final String uri) {
                    if (extensionManager != null) extensionManager.installFromPage(uri);
                }
                @Override public void onNewTabRequested(final String uri) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override public void run() {
                            Uri requested = Uri.parse(uri);
                            if (!"https".equalsIgnoreCase(requested.getScheme()) || requested.getHost() == null || requested.getUserInfo() != null) return;
                            if (browserTabs.size() >= 12) {
                                Toast.makeText(MainActivity.this, "Você pode abrir até 12 abas", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            createBrowserTab(uri);
                        }
                    });
                }
                @Override public void onContextMenu(int x, int y, GeckoSession.ContentDelegate.ContextElement element) {
                    showWebContextMenu(x, y, element);
                }
                @Override public void onExternalResponse(WebResponse response) {
                    handleGeckoDownload(response);
                }
                @Override public void onFullScreenChanged(boolean fullScreen) {
                    getWindow().getDecorView().setSystemUiVisibility(fullScreen
                        ? View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        : View.SYSTEM_UI_FLAG_VISIBLE);
                }
                @Override public void onContentProcessCrashed() {
                    logDiagnostic("Processo de conteúdo Gecko encerrou inesperadamente");
                    if (geckoEngine != null && geckoEngine.getCurrentUri() != null && geckoEngine.getCurrentUri().startsWith("https://")) geckoEngine.reload();
                    finishPageProgress();

                    Toast.makeText(MainActivity.this, "A página encontrou um problema. Tente recarregar.", Toast.LENGTH_LONG).show();
                }
                @Override public void onScrollChanged(int scrollY) { geckoScrollY = Math.max(0, scrollY); }
            },
            javaScriptEnabled,
            Dados.getBoolean("tracker_blocking", true));
        extensionManager = new ExtensionManager(this, GeckoEngineController.getRuntime(),
            geckoEngine.getSession(), url -> createBrowserTab(url));
        installGeckoPermissionDelegate();
        if (browserTabs.isEmpty()) browserTabs.add(new BrowserTab("Nova aba", pendingRestoreUri, null));
        persistBrowserTabs();
        updateTabCounter();
        geckoEngine.getSession().setTranslationsSessionDelegate(
            new TranslationsController.SessionTranslation.Delegate() {
                @Override public void onTranslationStateChange(
                    GeckoSession session,
                    TranslationsController.SessionTranslation.TranslationState state
                ) {
                    if (state != null && state.detectedLanguages != null
                        && state.detectedLanguages.docLangTag != null) {
                        detectedPageLanguage = state.detectedLanguages.docLangTag;
                    }
                }
            });
        if (pendingSessionState != null) {
            geckoEngine.restoreState(pendingSessionState, pendingRestoreUri);
            pendingSessionState = null;
        } else if (pendingRestoreUri != null && pendingRestoreUri.startsWith("https://")) {
            loadUrlWithPrivacyHeaders(pendingRestoreUri);
        } else if (!browserTabs.isEmpty()) {
            BrowserTab activeTab = browserTabs.get(activeBrowserTab);
            if (activeTab.url != null && activeTab.url.startsWith("https://"))
                loadUrlWithPrivacyHeaders(activeTab.url);
        }
        pendingRestoreUri = null;
        installSelectionActions();
        setupPullToRefresh();
    }
	
	
	public void _armazenamento() {
		boolean dimmed = "ativado".equals(Dados.getString("brilho", ""));
		checkbox_brilho.setChecked(dimmed);
		geckoView.setAlpha(dimmed ? 0.5f : 1f);
		javaScriptEnabled = !"desativado".equals(Dados.getString("java", "ativado"));
		checkbox_javascript2.setChecked(javaScriptEnabled);
		if (geckoEngine != null) geckoEngine.setJavaScriptEnabled(javaScriptEnabled);
		vazio.setVisibility(View.VISIBLE);
	}
	
	
		
	
		
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}

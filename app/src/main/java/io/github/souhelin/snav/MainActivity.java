package io.github.souhelin.snav;

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
import android.widget.*;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
	private GeckoSession.PermissionDelegate.MediaCallback pendingMediaPermissionCallback;
	private GeckoSession.PermissionDelegate.MediaSource[] pendingVideoSources;
	private GeckoSession.PermissionDelegate.MediaSource[] pendingAudioSources;
	private String pendingMediaPermissionOrigin;
	private WebResponse pendingGeckoDownload;
	private String pendingGeckoDownloadName;
	private final java.util.concurrent.ExecutorService downloadExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
	
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
	private CircularProgressIndicator refreshIndicator;
	private TextView text_barra;
	private ImageButton button_site_info;
	private ImageButton button_menu;
	private ProgressBar prog;
	private TextView text1;
	private TextView text2;
	private ScrollView vscroll1;
	private LinearLayout linear33;
	private LinearLayout linear_tela3_base;
	private LinearLayout linearmenu2;
	private LinearLayout linear_escurecer;
	private LinearLayout linear_JavaScript;
	private LinearLayout linear_limpar_dados2;
	private HorizontalScrollView hscroll1;
	private LinearLayout linear25;
	private LinearLayout linear_copiar_URL;
	private LinearLayout linear_tradutor_google;
	private LinearLayout linear_modo_leitura;
	private LinearLayout linear_abrir_no_navegador;
	private ImageView imageview57;
	private TextView text14;
	private ImageView imageview63;
	private TextView text19;
	private ImageView imageview92;
	private TextView text7;
	private ImageView imageview58;
	private TextView text15;
	private MaterialSwitch checkbox_brilho;
	private MaterialSwitch checkbox_wallpaper;
    private LinearLayout homeShortcuts;
    private View pageErrorView;
    private boolean stoppedByUser;
    private boolean toolsOpenedFromBrowser;
    private final java.util.concurrent.ExecutorService faviconExecutor = java.util.concurrent.Executors.newFixedThreadPool(2);
    private final android.util.LruCache<String, Bitmap> faviconCache = new android.util.LruCache<>(24);
    private final Set<String> faviconRequests = new HashSet<>();
    private final Set<String> faviconAttempted = new HashSet<>();
	private UnsplashWallpaperManager wallpaperManager;
	private UnsplashWallpaperManager.Callback wallpaperCallback;
	private float pullStartY;
	private float pullStartX;
	private float pullDistance;
	private boolean pullTracking;
	private boolean pullConsumed;
	private MaterialSwitch checkbox_javascript2;
	private TextView text5;
	
	private Intent intent = new Intent();
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
		addDiagnosticMenuAction();
		showPendingCrashReport();
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
		if (browserContentContainer != null) {
			refreshIndicator = new CircularProgressIndicator(this);
			refreshIndicator.setIndicatorSize(dp(36));
			refreshIndicator.setTrackThickness(dp(3));
			refreshIndicator.setIndeterminate(false);
			refreshIndicator.setVisibility(View.GONE);
			FrameLayout.LayoutParams refreshParams = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			refreshParams.topMargin = dp(12);
			browserContentContainer.addView(refreshIndicator, refreshParams);
		}
		text_barra = findViewById(R.id.text_barra);
		button_site_info = findViewById(R.id.button_site_info);
		button_menu = findViewById(R.id.button_menu);
		prog = findViewById(R.id.prog);
		text1 = findViewById(R.id.text1);
		text2 = findViewById(R.id.text2);
		vscroll1 = findViewById(R.id.vscroll1);
		linear33 = findViewById(R.id.linear33);
		linear_tela3_base = findViewById(R.id.linear_tela3_base);
		linearmenu2 = findViewById(R.id.linearmenu2);
		linear_escurecer = findViewById(R.id.linear_escurecer);
		linear_JavaScript = findViewById(R.id.linear_JavaScript);
		linear_limpar_dados2 = findViewById(R.id.linear_limpar_dados2);
		hscroll1 = findViewById(R.id.hscroll1);
		linear25 = findViewById(R.id.linear25);
		linear_copiar_URL = findViewById(R.id.linear_copiar_URL);
		linear_tradutor_google = findViewById(R.id.linear_tradutor_google);
		linear_modo_leitura = findViewById(R.id.linear_modo_leitura);
		linear_abrir_no_navegador = findViewById(R.id.linear_abrir_no_navegador);
		imageview57 = findViewById(R.id.imageview57);
		text14 = findViewById(R.id.text14);
		imageview63 = findViewById(R.id.imageview63);
		text19 = findViewById(R.id.text19);
		imageview92 = findViewById(R.id.imageview92);
		text7 = findViewById(R.id.text7);
		imageview58 = findViewById(R.id.imageview58);
		text15 = findViewById(R.id.text15);
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
					tela1.setBackgroundColor(getResources().getColor(R.color.snav_background));
					text_icon.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
				showSnavDecisionSheet("Limpar dados de navegação",
					"Você deseja remover os dados de navegação armazenados pelo Snav?",
					"Limpar", "Cancelar", new Runnable() {
						@Override public void run() { clearGeckoBrowsingData(); }
					}, null);
			}
		});
		
		linear_copiar_URL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				SketchwareUtil.showMessage(getApplicationContext(), "Copiado para a área de transferência");
				((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", (geckoEngine == null ? null : geckoEngine.getCurrentUri())));
			}
		});
		
		linear_tradutor_google.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) { showTranslationBottomSheet(); }
		});
		
		linear_modo_leitura.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) { showReaderOptionsBottomSheet(); }
		});
		
		linear_abrir_no_navegador.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(geckoEngine.getCurrentUri()));
				startActivity(intent);
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
		action.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
		showSnavDecisionSheet("Falha detectada no Snav",
			"O Snav detectou que a execução anterior terminou inesperadamente. Você pode copiar o diagnóstico para ajudar a identificar a causa.",
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
			Toast.makeText(this, "Não foi possível copiar o diagnóstico.", Toast.LENGTH_LONG).show();
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
		logDiagnostic("Ciclo de vida: onResume");
	
		clearGrantedPermissionDenials();}

	@Override
	protected void onPause() {
		if (geckoEngine != null) geckoEngine.setActive(false);
		logDiagnostic("Ciclo de vida: onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logDiagnostic("Ciclo de vida: onDestroy; isFinishing=" + isFinishing());
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
		if (downloadExecutor != null) downloadExecutor.shutdown();
		if (translationThread != null) translationThread.quitSafely();
        faviconExecutor.shutdownNow();
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
		if (refreshIndicator != null) refreshIndicator.setVisibility(View.GONE);
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
		title.setTextColor(getResources().getColor(R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		LinearLayout.LayoutParams translationTitleParams = new LinearLayout.LayoutParams(-1, -2);
		translationTitleParams.bottomMargin = dp(8);
		content.addView(title, translationTitleParams);
		TextView subtitle = new TextView(this);
		subtitle.setText("A tradução é processada pelo mecanismo do GeckoView com modelos de idioma. Os modelos necessários podem ser baixados na primeira utilização.");
		subtitle.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
		status.setText("Carregando idiomas disponíveis…");
		status.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		status.setTextSize(12);
		status.setPadding(0, dp(8), 0, dp(12));
		content.addView(status);
		LinearLayout actions = new LinearLayout(this);
		actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
		MaterialButton restore = new MaterialButton(this);
		restore.setText("Original");
		restore.setAllCaps(false);
		restore.setTextColor(getResources().getColor(R.color.snav_on_surface));
		restore.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_surface)));
		restore.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_outline)));
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
										status.setText("O mecanismo não disponibilizou idiomas para este dispositivo.");
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
										? "Selecione o idioma de origem."
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
									if (!isFinishing() && !isDestroyed() && sheet.isShowing()) status.setText("Não foi possível carregar os idiomas. Verifique a disponibilidade do mecanismo de tradução.");
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
					status.setText("Selecione os dois idiomas.");
					return;
				}
				final String from = sourceLanguages.get(fromIndex).code;
				final String to = targetLanguages.get(toIndex).code;
				if (from.equalsIgnoreCase(to)) {
					status.setText("Escolha idiomas diferentes.");
					return;
				}
				status.setText("Iniciando tradução e preparando modelos…");
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
											status.setText("Falha ao iniciar tradução. O par de idiomas pode não estar disponível.");
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
										@Override public void run() { status.setText("A página já está no idioma original ou não foi possível restaurá-la."); }
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
			Toast.makeText(this, "Abra uma página HTTPS para usar o modo leitura", Toast.LENGTH_SHORT).show();
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
		title.setTextColor(getResources().getColor(R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		content.addView(title);
		TextView description = new TextView(this);
		description.setText("Extrai o conteúdo principal da página e apresenta o texto em uma visualização limpa, sem navegar para uma página interna do navegador.");
		description.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
			Toast.makeText(this, "Não foi possível preparar esta página para leitura", Toast.LENGTH_LONG).show();
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
		page.setBackgroundColor(getResources().getColor(R.color.snav_surface));

		TextView eyebrow = new TextView(this);
		eyebrow.setText("MODO DE LEITURA");
		eyebrow.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		eyebrow.setTextSize(12);
		eyebrow.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		page.addView(eyebrow);

		String heading = currentPageTitle == null || currentPageTitle.trim().isEmpty()
			? Uri.parse(sourceUri).getHost() : currentPageTitle.trim();
		TextView title = new TextView(this);
		title.setText(heading == null ? "Artigo" : heading);
		title.setTextColor(getResources().getColor(R.color.snav_on_surface));
		title.setTextSize(25);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setLineSpacing(dp(2), 1.05f);
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
		titleParams.topMargin = dp(10);
		page.addView(title, titleParams);

		TextView source = new TextView(this);
		source.setText(Uri.parse(sourceUri).getHost() == null ? sourceUri : Uri.parse(sourceUri).getHost());
		source.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
		body.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
		if (linear_modo_leitura != null) linear_modo_leitura.setAlpha(0.65f);
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
		if (linear_modo_leitura != null) linear_modo_leitura.setAlpha(1f);
	}

	private int siteInfoDp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void addSiteInfoRow(LinearLayout parent, String title, String value) {
		TextView heading = new TextView(this);
		heading.setText(title);
		heading.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		heading.setTextSize(12);
		parent.addView(heading);

		TextView detail = new TextView(this);
		detail.setText(value);
		detail.setTextColor(getResources().getColor(R.color.snav_on_surface));
		detail.setTextSize(14);
		detail.setTextIsSelectable(true);
		detail.setPadding(0, siteInfoDp(3), 0, siteInfoDp(12));
		parent.addView(detail);
	}


    private void showScreen(int screen) {
        tela1.setVisibility(screen == 1 ? View.VISIBLE : View.GONE);
        tela2.setVisibility(screen == 2 ? View.VISIBLE : View.GONE);
        tela3.setVisibility(screen == 3 ? View.VISIBLE : View.GONE);
        linear_b.setVisibility(screen == 3 ? View.GONE : View.VISIBLE);
        if (screen != 1 && edittext1 != null) {
            edittext1.clearFocus();
            android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(edittext1.getWindowToken(), 0);
        }
        if (screen == 1) refreshHomeShortcuts();
    }

    private void returnFromTools() {
        showScreen(toolsOpenedFromBrowser ? 2 : 1);
    }

    private void setupPageErrorView() {
        LinearLayout error = new LinearLayout(this);
        error.setOrientation(LinearLayout.VERTICAL);
        error.setGravity(Gravity.CENTER);
        error.setPadding(dp(24), dp(24), dp(24), dp(24));
        error.setBackgroundColor(getResources().getColor(R.color.snav_surface));
        TextView message = new TextView(this);
        message.setText("Não foi possível carregar esta página. Verifique sua conexão e tente novamente.");
        message.setGravity(Gravity.CENTER);
        message.setTextColor(getResources().getColor(R.color.snav_on_surface));
        message.setTextSize(16);
        error.addView(message);
        MaterialButton retry = new MaterialButton(this);
        retry.setText("Tentar novamente");
        retry.setAllCaps(false);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                pageErrorView.setVisibility(View.GONE);
                if (geckoEngine != null) geckoEngine.reload();
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
        List<BrowserLibraryStore.Entry> pinned = browserLibraryStore.getPinned();
        List<BrowserLibraryStore.Entry> recent = browserLibraryStore.getHistory();
        if (!pinned.isEmpty()) addShortcutSection("Fixados", pinned, true);
        else {
            TextView hint = new TextView(this);
            hint.setText("Fixe até 4 páginas pelo menu ou mantendo um recente pressionado.");
            hint.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
            hint.setTextSize(12);
            hint.setGravity(Gravity.CENTER);
            homeShortcuts.addView(hint);
        }
        if (!recent.isEmpty()) addShortcutSection("Recentes", recent, false);
    }

    private void addShortcutSection(String title, List<BrowserLibraryStore.Entry> entries, final boolean pinned) {
        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextSize(14);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
            MaterialButton icon = new MaterialButton(this);
            icon.setAllCaps(false);
            icon.setText(name.substring(0, 1).toUpperCase(java.util.Locale.getDefault()));
            icon.setTextSize(20);
            icon.setInsetTop(0);
            icon.setInsetBottom(0);
            icon.setMinWidth(0);
            icon.setMinimumWidth(0);
            icon.setPadding(0, 0, 0, 0);
            icon.setCornerRadius(dp(28));
            icon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_switch_primary_container)));
            icon.setTextColor(getResources().getColor(R.color.snav_on_surface));
            icon.setContentDescription("Abrir " + name + ". Toque e segure para " + (pinned ? "desafixar" : "fixar") + ".");
            item.addView(icon, new LinearLayout.LayoutParams(dp(56), dp(56)));
            bindFavicon(icon, entry.url);
            TextView label = new TextView(this);
            label.setText(name);
            label.setTextSize(12);
            label.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
                    if (pinned) {
                        browserLibraryStore.unpin(entry.url);
                        Toast.makeText(MainActivity.this, "Página desafixada", Toast.LENGTH_SHORT).show();
                    } else if (browserLibraryStore.pin(entry.title, entry.url)) {
                        Toast.makeText(MainActivity.this, "Página fixada", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(MainActivity.this, "Você pode fixar até 4 páginas", Toast.LENGTH_SHORT).show();
                    refreshHomeShortcuts();
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

    // Request only the visited origin's conventional icon, without cookies or redirects.
    // A letter remains visible when a site has no decodable icon.
    private void bindFavicon(final MaterialButton button, String pageUrl) {
        if (!isSecureOrigin(pageUrl)) return;
        Uri page = Uri.parse(pageUrl);
        final String iconUrl = new Uri.Builder().scheme("https").encodedAuthority(page.getEncodedAuthority()).path("/favicon.ico").build().toString();
        Bitmap cached = faviconCache.get(iconUrl);
        if (cached != null) { setShortcutIcon(button, cached); return; }
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

    private void setShortcutIcon(MaterialButton button, Bitmap bitmap) {
        button.setText("");
        button.setIcon(new BitmapDrawable(getResources(), bitmap));
        button.setIconTint(null);
        button.setIconSize(dp(32));
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        button.setGravity(Gravity.CENTER);
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
                browserTabs.add(new BrowserTab(item.optString("title", "Nova guia"),
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
                item.put("title", tab.title == null ? "Nova guia" : tab.title);
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
        BrowserTab target = browserTabs.get(index);
        readerRequestId++; readerModePending = false; readerMode = false; readerOriginalUri = null;
        if (target.state != null) geckoEngine.restoreState(target.state);
        else if (target.url != null && target.url.startsWith("https://")) loadUrlWithPrivacyHeaders(target.url);
        else geckoEngine.loadUri("about:blank", false);
        currentPageTitle = target.title; text_barra.setText(domainForAddressBar(target.url));
        vazio.setVisibility(target.url == null || target.url.isEmpty() || "about:blank".equals(target.url) ? View.VISIBLE : View.GONE);
        updateTabCounter(); persistBrowserTabs();
        showScreen(target.url == null || "about:blank".equals(target.url) ? 1 : 2);
    }

    private void createBrowserTab() {
        captureActiveTabState();
        if (browserTabs.size() >= 12) { Toast.makeText(this, "Limite de 12 abas atingido", Toast.LENGTH_SHORT).show(); return; }
        browserTabs.add(new BrowserTab("Nova guia", null, null));
        int next = browserTabs.size() - 1;
        switchBrowserTab(next);
    }

    private void closeBrowserTab(int index) {
        if (browserTabs.size() <= 1) {
            BrowserTab only = browserTabs.get(0); only.title = "Nova guia"; only.url = null; only.state = null;
            currentPageTitle = null; activeBrowserTab = 0; text_barra.setText("Snav Browser"); vazio.setVisibility(View.VISIBLE); if (geckoEngine != null) geckoEngine.loadUri("about:blank", false);
        } else {
            boolean closingActive = index == activeBrowserTab;
            if (closingActive) captureActiveTabState();
            browserTabs.remove(index);
            if (index < activeBrowserTab) activeBrowserTab--;
            else if (closingActive) activeBrowserTab = Math.min(index, browserTabs.size() - 1);
            if (closingActive) {
                BrowserTab selected = browserTabs.get(activeBrowserTab);
                if (selected.state != null) geckoEngine.restoreState(selected.state);
                else if (selected.url != null && selected.url.startsWith("https://")) loadUrlWithPrivacyHeaders(selected.url);
                else geckoEngine.loadUri("about:blank", false);
                currentPageTitle = selected.title;
                text_barra.setText(domainForAddressBar(selected.url));
                vazio.setVisibility(selected.url == null || selected.url.isEmpty() || "about:blank".equals(selected.url) ? View.VISIBLE : View.GONE);
            }
        }
        updateTabCounter(); persistBrowserTabs(); showTabsBottomSheet();
    }

    private void performLoadStop() {
        if (pageLoading && geckoEngine != null) {
            stoppedByUser = true;
            geckoEngine.stop();
            finishPageProgress();
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
        BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
        root.addView(handle, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView heading = new TextView(this);
        heading.setText("Menu");
        heading.setTextSize(21);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(getResources().getColor(R.color.snav_on_surface));
        root.addView(heading);
        addMenuSheetRow(root, R.drawable.ms_tab, "Gerenciar abas", new Runnable() {
            @Override public void run() { sheet.dismiss(); showTabsBottomSheet(); }
        });
        addMenuSheetRow(root, R.drawable.ms_menu_book, "Biblioteca", new Runnable() {
            @Override public void run() { sheet.dismiss(); showBrowserLibraryBottomSheet(false); }
        });
        addMenuSheetRow(root, R.drawable.ms_refresh, "Carregar / interromper", new Runnable() {
            @Override public void run() { sheet.dismiss(); performLoadStop(); }
        });
        final String currentUrl = geckoEngine == null ? null : geckoEngine.getCurrentUri();
        if (isSecureOrigin(currentUrl)) {
            boolean pinned = false;
            for (BrowserLibraryStore.Entry entry : browserLibraryStore.getPinned()) {
                if (entry.url.equals(currentUrl)) pinned = true;
            }
            final boolean alreadyPinned = pinned;
            addMenuSheetRow(root, R.drawable.icon_add_round, pinned ? "Desafixar da tela inicial" : "Fixar na tela inicial", new Runnable() {
                @Override public void run() {
                    sheet.dismiss();
                    if (alreadyPinned) browserLibraryStore.unpin(currentUrl);
                    else if (!browserLibraryStore.pin(currentPageTitle, currentUrl)) {
                        Toast.makeText(MainActivity.this, "Você pode fixar até 4 páginas", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    refreshHomeShortcuts();
                }
            });
        }
        addMenuSheetRow(root, R.drawable.ms_build, "Ferramentas", new Runnable() {
            @Override public void run() {
                sheet.dismiss();
                toolsOpenedFromBrowser = tela2.getVisibility() == View.VISIBLE;
                showScreen(3);
            }
        });
        sheet.setContentView(root);
        sheet.show();
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
    }

    private void addMenuSheetRow(LinearLayout parent, int iconRes, String label, final Runnable action) {
        MaterialButton row = new MaterialButton(this, null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle);
        row.setAllCaps(false);
        row.setText(label);
        row.setTextSize(14);
        row.setTextColor(getResources().getColor(R.color.snav_on_surface));
        row.setIconResource(iconRes);
        row.setIconTint(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_on_surface_variant)));
        row.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        row.setStrokeWidth(0);
        row.setCornerRadius(dp(16));
        row.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_surface)));
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { action.run(); }
        });
        parent.addView(row, new LinearLayout.LayoutParams(-1, dp(56)));
    }

    private void showTabsBottomSheet() {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22), dp(18), dp(22), dp(24));
        BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this); root.addView(handle, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView heading = new TextView(this); heading.setText("Abas abertas · " + browserTabs.size()); heading.setTextSize(21); heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD); heading.setTextColor(getResources().getColor(R.color.snav_on_surface)); root.addView(heading);
        addLibraryAction(root, "Nova aba", new Runnable() { @Override public void run() { sheet.dismiss(); createBrowserTab(); } });
        ScrollView scroll = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list); root.addView(scroll, new LinearLayout.LayoutParams(-1, dp(340)));
        for (int i = 0; i < browserTabs.size(); i++) {
            final int index = i; final BrowserTab tab = browserTabs.get(i);
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView label = new TextView(this); String name = tab.title == null || tab.title.trim().isEmpty() ? "Nova guia" : tab.title;
            label.setText((i == activeBrowserTab ? "●  " : "") + name + "\n" + (tab.url == null ? "Página em branco" : domainForAddressBar(tab.url)));
            label.setTextColor(getResources().getColor(R.color.snav_on_surface)); label.setTextSize(14); label.setPadding(dp(8), dp(14), dp(8), dp(14));
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            TextView close = new TextView(this); close.setText("×"); close.setTextSize(24); close.setGravity(Gravity.CENTER); close.setTextColor(getResources().getColor(R.color.snav_on_surface_variant)); row.addView(close, new LinearLayout.LayoutParams(dp(48), dp(52)));
            label.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sheet.dismiss(); switchBrowserTab(index); } });
            close.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sheet.dismiss(); closeBrowserTab(index); } });
            list.addView(row);
        }
        sheet.setContentView(root); sheet.show(); sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED); sheet.getBehavior().setSkipCollapsed(true);
    }

    private void addLibraryAction(LinearLayout parent, String label, final Runnable action) {
        TextView button = new TextView(this);
        button.setText(label);
        button.setTextColor(getResources().getColor(R.color.snav_primary));
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
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
        root.addView(handle, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView heading = new TextView(this);
        heading.setText("Biblioteca");
        heading.setTextSize(21);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(getResources().getColor(R.color.snav_on_surface));
        root.addView(heading);
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView favorites = new TextView(this);
        favorites.setText("Favoritos");
        TextView history = new TextView(this);
        history.setText("Histórico");
        for (TextView tab : new TextView[] { favorites, history }) {
            tab.setTextSize(14);
            tab.setTextColor(getResources().getColor(R.color.snav_primary));
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(12), dp(14), dp(12), dp(14));
            actions.addView(tab, new LinearLayout.LayoutParams(0, dp(48), 1));
        }
        root.addView(actions);
        final LinearLayout entries = new LinearLayout(this);
        entries.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.addView(entries, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, dp(360)));
        final Runnable[] render = new Runnable[1];
        final boolean[] showingHistory = new boolean[] { historyMode };
        render[0] = new Runnable() {
            @Override public void run() {
                entries.removeAllViews();
                List<BrowserLibraryStore.Entry> data = showingHistory[0]
                    ? browserLibraryStore.getHistory() : browserLibraryStore.getBookmarks();
                if (data.isEmpty()) {
                    TextView empty = new TextView(MainActivity.this);
                    empty.setText(showingHistory[0] ? "Seu histórico está vazio." : "Nenhum favorito salvo.");
                    empty.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
                            groupHeading.setTextColor(getResources().getColor(R.color.snav_primary));
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
                    name.setTextColor(getResources().getColor(R.color.snav_on_surface));
                    name.setTextSize(15);
                    name.setMaxLines(1);
                    TextView address = new TextView(MainActivity.this);
                    address.setText(item.url);
                    address.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
                                Toast.makeText(MainActivity.this, showingHistory[0] ? "Registro removido" : "Favorito removido", Toast.LENGTH_SHORT).show();
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
        addLibraryAction(root, "Gerenciador de downloads", new Runnable() {
            @Override public void run() { sheet.dismiss(); showDownloadsBottomSheet(); }
        });
        addLibraryAction(root, "Exportar favoritos", new Runnable() {
            @Override public void run() { exportBookmarks(); }
        });
        addLibraryAction(root, "Importar favoritos", new Runnable() {
            @Override public void run() { importBookmarks(); }
        });
        addLibraryAction(root, "Limpar histórico", new Runnable() {
            @Override public void run() {
                browserLibraryStore.clearHistory();
                refreshHomeShortcuts();
                showingHistory[0] = true;
                render[0].run();
                Toast.makeText(MainActivity.this, "Histórico apagado", Toast.LENGTH_SHORT).show();
            }
        });
        render[0].run();
        sheet.setContentView(root);
        sheet.show();
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
    }

    private void showDownloadsBottomSheet() {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(18), dp(22), dp(24));
        BottomSheetDragHandleView handle = new BottomSheetDragHandleView(this);
        root.addView(handle, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView heading = new TextView(this);
        heading.setText("Downloads");
        heading.setTextSize(21);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextColor(getResources().getColor(R.color.snav_on_surface));
        root.addView(heading);
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
            legacy.setText("Permita o acesso aos arquivos para listar downloads neste Android.");
            legacy.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
            empty.setText("Nenhum download encontrado.");
            empty.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
            empty.setPadding(0, dp(18), 0, dp(18));
            list.addView(empty);
        }
        sheet.setContentView(root);
        sheet.show();
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.getBehavior().setSkipCollapsed(true);
    }

    private void addDownloadFileRow(final LinearLayout list, final Uri itemUri, String name, long size) {
        final TextView item = new TextView(this);
        item.setText((name == null ? "Arquivo" : name) + "\n"
            + android.text.format.Formatter.formatShortFileSize(this, size));
        item.setTextColor(getResources().getColor(R.color.snav_on_surface));
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
                catch (ActivityNotFoundException error) { Toast.makeText(MainActivity.this, "Nenhum aplicativo pode abrir este arquivo", Toast.LENGTH_LONG).show(); }
            }
        });
        item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Excluir download")
                    .setMessage("Deseja excluir este arquivo da pasta Downloads?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            try {
                                int deleted = getContentResolver().delete(itemUri, null, null);
                                if (deleted > 0) {
                                    list.removeView(item);
                                    if (list.getChildCount() == 0) {
                                        TextView empty = new TextView(MainActivity.this);
                                        empty.setText("Nenhum download encontrado.");
                                        empty.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
        catch (ActivityNotFoundException error) { Toast.makeText(this, "Seletor de arquivos indisponível", Toast.LENGTH_LONG).show(); }
    }

    private void exportBookmarks() {
        Intent picker = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("text/html");
        picker.putExtra(Intent.EXTRA_TITLE, "snav-bookmarks.html");
        try { startActivityForResult(picker, REQUEST_EXPORT_BOOKMARKS); }
        catch (ActivityNotFoundException error) { Toast.makeText(this, "Seletor de arquivos indisponível", Toast.LENGTH_LONG).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null || browserLibraryStore == null) return;
        Uri uri = data.getData();
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
		title.setTextColor(getResources().getColor(R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setPadding(0, 0, 0, siteInfoDp(18));
		content.addView(title);
		addSiteInfoRow(content, "ENDEREÇO COMPLETO", currentUrl == null ? "Nenhum endereço aberto" : currentUrl);
        addLibraryAction(content, "Salvar nos favoritos", new Runnable() {
            @Override public void run() {
                if (browserLibraryStore != null && browserLibraryStore.addBookmark(currentPageTitle, currentUrl))
                    Toast.makeText(MainActivity.this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();
                else Toast.makeText(MainActivity.this, "Não foi possível salvar este endereço", Toast.LENGTH_SHORT).show();
            }
        });
        addLibraryAction(content, "Favoritos e histórico", new Runnable() {
            @Override public void run() { sheet.dismiss(); showBrowserLibraryBottomSheet(false); }
        });
		addSiteInfoRow(content, "CONEXÃO", "https".equalsIgnoreCase(scheme) ? "HTTPS — conexão criptografada" : "about".equalsIgnoreCase(scheme) ? "Página interna do GeckoView" : "Não disponível");
		addSiteInfoRow(content, "PROTEÇÃO", "Proteção contra rastreamento integrada ao GeckoView e isolamento dinâmico de cookies de terceiros (dFPI).");
		addSiteInfoRow(content, "PERMISSÕES", "Câmera, microfone e localização exigem consentimento por origem HTTPS e autorização do Android quando aplicável.");
		addSiteInfoRow(content, "JAVASCRIPT", javaScriptEnabled ? "Ativado para o navegador" : "Desativado para o navegador");

		TextView clearData = new TextView(this);
		clearData.setText("Limpar dados deste site");
		clearData.setTextColor(Color.WHITE);
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
					Toast.makeText(MainActivity.this, "Não há domínio para limpar", Toast.LENGTH_SHORT).show();
					return;
				}
				sheet.dismiss();
				showSnavDecisionSheet("Limpar dados do site",
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
		managePermissions.setTextColor(Color.WHITE);
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
		} else if (requestCode == 4105) {
            boolean granted = grantResults != null && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            if (granted) showDownloadsBottomSheet();
            else Toast.makeText(this, "Permissão negada. Não foi possível listar os downloads.", Toast.LENGTH_LONG).show();
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
					Toast.makeText(this, "Permissão negada. Download cancelado.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

		private void addTrackerBlockingPreference(boolean enabled) {
		if (linear33 == null || linear33.findViewWithTag("snav_tracker_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_tracker_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Proteção contra rastreamento");
		toggle.setTextColor(getResources().getColor(R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Usa a proteção contra rastreamento integrada ao GeckoView.");
		summary.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
		int insertAt = Math.max(0, linear33.getChildCount() - 1);
		linear33.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
	}

	private void addDoNotTrackPreference(boolean enabled) {
		if (linear33 == null || linear33.findViewWithTag("snav_dnt_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_dnt_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Do Not Track");
		toggle.setTextColor(getResources().getColor(R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Envia DNT: 1 nas navegações iniciadas pelo Snav; sites não são obrigados a respeitar.");
		summary.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
		int insertAt = Math.max(0, linear33.getChildCount() - 1);
		linear33.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
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
		try { geckoEngine.loadUri(url, Dados.getBoolean("do_not_track", false)); }
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
		title.setTextColor(getResources().getColor(R.color.snav_on_surface));
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
		titleParams.bottomMargin = dp(12);
		content.addView(title, titleParams);

		View divider = new View(this);
		divider.setBackgroundColor(getResources().getColor(R.color.snav_outline));
		content.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

		TextView message = new TextView(this);
		message.setText(messageText);
		message.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
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
		negative.setTextColor(getResources().getColor(R.color.snav_on_surface));
		negative.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_surface)));
		negative.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_outline)));
		negative.setStrokeWidth(dp(1));
		negative.setCornerRadius(dp(20));
		MaterialButton positive = new MaterialButton(this);
		positive.setText(positiveText);
		positive.setAllCaps(false);
		positive.setTextColor(Color.WHITE);
		positive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.snav_accent)));
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
			"O Snav não envia dados por HTTP. A conexão com " + host + " não é criptografada. Deseja tentar abrir a versão HTTPS?",
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
				if (!isSecureOrigin(permission.uri)
					|| permission.permission != GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION) {
					return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY);
				}
				final GeckoResult<Integer> result = new GeckoResult<Integer>();
				permission.notifyShown();
				Uri origin = Uri.parse(permission.uri);
				showSnavDecisionSheet("Permitir localização?",
					"O site " + origin.getHost() + " solicita acesso à sua localização. Permita somente se iniciou essa ação.",
					"Permitir", "Bloquear", new Runnable() {
						@Override public void run() { result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW); }
					}, new Runnable() {
						@Override public void run() { result.complete(GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY); }
					});
				return result;
			}

			@Override
			public void onMediaPermissionRequest(GeckoSession session, String uri,
				GeckoSession.PermissionDelegate.MediaSource[] video,
				GeckoSession.PermissionDelegate.MediaSource[] audio,
				GeckoSession.PermissionDelegate.MediaCallback callback) {
				boolean hasVideo = video != null && video.length > 0;
				boolean hasAudio = audio != null && audio.length > 0;
				if (!isSecureOrigin(uri) || (!hasVideo && !hasAudio)) {
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

	private void showGeckoMediaPermissionPrompt(String uri,
		GeckoSession.PermissionDelegate.MediaSource[] video,
		GeckoSession.PermissionDelegate.MediaSource[] audio,
		final GeckoSession.PermissionDelegate.MediaCallback callback) {
		boolean hasVideo = video != null && video.length > 0;
		boolean hasAudio = audio != null && audio.length > 0;
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
		showSnavDecisionSheet("Permissão de mídia",
			host + " solicita acesso à câmera e/ou ao microfone. Permita somente se iniciou uma chamada ou gravação.",
			"Permitir", "Bloquear", new Runnable() {
				@Override public void run() { callback.grant(hasVideo ? video[0] : null, hasAudio ? audio[0] : null); }
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
			Toast.makeText(this, "Não há aplicativo disponível para abrir este endereço", Toast.LENGTH_SHORT).show();
		}
	}

		private void clearGeckoBrowsingData() {
		if (geckoEngine == null) return;
		geckoEngine.clearBrowsingData().accept(
			new GeckoResult.Consumer<Void>() {
				@Override public void accept(Void ignored) {
                    browserLibraryStore.clearHistory();
                    refreshHomeShortcuts();
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
			Toast.makeText(this, "Download bloqueado: resposta inválida ou conexão não segura.", Toast.LENGTH_LONG).show();
			return;
		}
		Uri uri = Uri.parse(response.uri);
		if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
			closeResponseBody(response);
			Toast.makeText(this, "Download bloqueado: somente HTTPS é permitido.", Toast.LENGTH_LONG).show();
			return;
		}
		String disposition = responseHeader(response, "Content-Disposition");
		final String name = guessDownloadFileName(response.uri, disposition);
		response.setReadTimeoutMillis(120000);
		showSnavDecisionSheet("Download",
			"Deseja salvar " + name + " na pasta Downloads?",
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

	private void saveGeckoDownload(final WebResponse response, final String fileName) {
		if (downloadExecutor == null || downloadExecutor.isShutdown()) {
			closeResponseBody(response);
			return;
		}
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
						while ((read = input.read(buffer)) != -1) stream.write(buffer, 0, read);
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
					runOnUiThread(new Runnable() {
						@Override public void run() {
							if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Download salvo em Downloads", Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception error) {
					if (mediaUri != null) getContentResolver().delete(mediaUri, null, null);
					if (legacyFile != null && legacyFile.exists()) legacyFile.delete();
					closeResponseBody(response);
					logDiagnostic("Falha ao salvar download Gecko: " + error.getClass().getSimpleName());
					runOnUiThread(new Runnable() {
						@Override public void run() {
							if (!isFinishing() && !isDestroyed()) Toast.makeText(MainActivity.this, "Falha ao salvar o download", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
			});
		} catch (java.util.concurrent.RejectedExecutionException error) {
			closeResponseBody(response);
		}
	}
	private void setupUnsplashWallpaper() {
		if (checkbox_wallpaper == null) return;
		if (!checkbox_wallpaper.isChecked()) {
			tela1.setBackgroundColor(getResources().getColor(R.color.snav_background));
			text_icon.setTextColor(getResources().getColor(R.color.snav_on_surface));
			return;
		}
		text_icon.setTextColor(getResources().getColor(R.color.snav_on_surface));
		if (wallpaperManager == null) wallpaperManager = new UnsplashWallpaperManager(this);
		wallpaperCallback = new UnsplashWallpaperManager.Callback() {
			@Override
			public void onWallpaperReady(Bitmap bitmap) {
				if (isFinishing() || isDestroyed() || checkbox_wallpaper == null || !checkbox_wallpaper.isChecked()) {
					if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
					return;
				}
				tela1.setBackground(new UnsplashWallpaperDrawable(bitmap));
				text_icon.setTextColor(Color.WHITE);
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
		if (geckoView == null || refreshIndicator == null) return;
		geckoView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
		final float refreshThreshold = getDip(96);
		geckoView.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View view, MotionEvent event) {
				int action = event.getActionMasked();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						pullStartY = event.getY();
						pullStartX = event.getX();
						pullDistance = 0f;
						pullTracking = geckoScrollY <= 0 && !pageLoading;
						pullConsumed = false;
						refreshIndicator.setVisibility(View.GONE);
						return false;

					case MotionEvent.ACTION_MOVE:
						if (!pullTracking || pageLoading || geckoScrollY > 0) return false;
						float dx = event.getX() - pullStartX;
						float dy = event.getY() - pullStartY;
						pullDistance = dy;
						if (dy <= 0f) {
							refreshIndicator.setVisibility(View.GONE);
							return false;
						}
					if (Math.abs(dx) > touchSlop && Math.abs(dx) > dy * 0.8f) {
						pullTracking = false;
						pullConsumed = false;
						refreshIndicator.setVisibility(View.GONE);
						return false;
					}
					if (dy < touchSlop || dy < Math.abs(dx) * 1.25f) return false;
					pullConsumed = true;
					refreshIndicator.setVisibility(View.VISIBLE);
					refreshIndicator.setIndeterminate(false);
					refreshIndicator.setProgressCompat(Math.min(100, Math.round((dy / refreshThreshold) * 100f)), false);
					refreshIndicator.setTranslationY(Math.max(dp(8), Math.min(dp(72), dy * 0.45f)));
					return true;

					case MotionEvent.ACTION_POINTER_DOWN:
						pullTracking = false;
						pullConsumed = false;
						refreshIndicator.setVisibility(View.GONE);
						return false;

					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						boolean refresh = action == MotionEvent.ACTION_UP
							&& pullTracking && pullConsumed && !pageLoading
							&& geckoScrollY <= 0 && pullDistance >= refreshThreshold;
						boolean consumed = pullConsumed;
						pullTracking = false;
						pullConsumed = false;
						if (refresh && geckoEngine != null && geckoEngine.getCurrentUri() != null) {
							refreshIndicator.setIndeterminate(true);
							refreshIndicator.setTranslationY(dp(24));
							refreshIndicator.setVisibility(View.VISIBLE);
							geckoEngine.reload();
							return true;
						}
					refreshIndicator.setVisibility(View.GONE);
					return consumed;
				}
				return false;
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
		//visibilidade
		//telas
		tela2.setVisibility(View.GONE);
		tela3.setVisibility(View.GONE);
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
		text7.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text14.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text15.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		text19.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		//checkbox
		checkbox_javascript2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		checkbox_brilho.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
	}
	
	
	public void _interfaces() {
		// A barra mantém o RippleDrawable definido no XML.
		linear_barra_pesquisa.setElevation(0);
		
		// vscroll1
		vscroll1.setVerticalScrollBarEnabled(false); vscroll1.setHorizontalScrollBarEnabled(false);
		// hscroll1
		hscroll1.setVerticalScrollBarEnabled(false); hscroll1.setHorizontalScrollBarEnabled(false);
	}
	
	
	public void _internet() {
        prog.setVisibility(View.GONE);
        prog.setAlpha(1f);
        prog.setProgress(0);
        prog.setIndeterminate(true);
        addTrackerBlockingPreference(Dados.getBoolean("tracker_blocking", true));
        addDoNotTrackPreference(Dados.getBoolean("do_not_track", false));
        javaScriptEnabled = !"desativado".equals(Dados.getString("java", "ativado"));
        restorePersistedBrowserTabs();
        geckoView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        geckoEngine = new GeckoEngineController(this, geckoView,
            new GeckoEngineController.Listener() {
                @Override public void onPageStarted(String uri) {
                    logDiagnostic("Navegação Gecko iniciada: " + safeHostForDiagnostics(uri));
                    beginPageProgress();
                    vazio.setVisibility(View.GONE);
                    stoppedByUser = false;
                    if (pageErrorView != null) pageErrorView.setVisibility(View.GONE);
                }
                @Override public void onPageStopped(boolean success) {
                    String uri = geckoEngine == null ? null : geckoEngine.getCurrentUri();
                    finishPageProgress();

                    vazio.setVisibility(uri == null || uri.isEmpty() || "about:blank".equals(uri) ? View.VISIBLE : View.GONE);
                    if (success && pageErrorView != null) pageErrorView.setVisibility(View.GONE);
                    if (!success && !stoppedByUser && isSecureOrigin(uri)) {
                        logDiagnostic("Falha de carregamento Gecko: " + safeHostForDiagnostics(uri));
                        if (pageErrorView != null) pageErrorView.setVisibility(View.VISIBLE);
                    }
                    stoppedByUser = false;
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
                @Override public void onNewTabRequested(final String uri) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override public void run() {
                            Uri requested = Uri.parse(uri);
                            if (!"https".equalsIgnoreCase(requested.getScheme()) || requested.getHost() == null || requested.getUserInfo() != null) return;
                            if (browserTabs.size() >= 12) {
                                Toast.makeText(MainActivity.this, "Limite de 12 abas atingido", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            createBrowserTab();
                            if (browserTabs.isEmpty()) return;
                            BrowserTab activeTab = browserTabs.get(activeBrowserTab);
                            activeTab.url = uri;
                            activeTab.title = "Nova guia";
                            persistBrowserTabs();
                            loadUrlWithPrivacyHeaders(uri);
                            vazio.setVisibility(View.GONE);
                            showScreen(2);
                        }
                    });
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
        installGeckoPermissionDelegate();
        if (browserTabs.isEmpty()) browserTabs.add(new BrowserTab("Nova guia", pendingRestoreUri, null));
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
            geckoEngine.restoreState(pendingSessionState);
            pendingSessionState = null;
        } else if (pendingRestoreUri != null && pendingRestoreUri.startsWith("https://")) {
            loadUrlWithPrivacyHeaders(pendingRestoreUri);
        } else if (!browserTabs.isEmpty()) {
            BrowserTab activeTab = browserTabs.get(activeBrowserTab);
            if (activeTab.url != null && activeTab.url.startsWith("https://"))
                loadUrlWithPrivacyHeaders(activeTab.url);
        }
        pendingRestoreUri = null;
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

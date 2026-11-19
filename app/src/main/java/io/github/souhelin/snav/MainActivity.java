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
import android.webkit.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.motion.MotionUtils;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import android.view.VelocityTracker;
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
	private static final String[] TRACKER_DOMAINS = new String[] {
		"doubleclick.net", "googlesyndication.com", "google-analytics.com",
		"googletagmanager.com", "adservice.google.com", "connect.facebook.net",
		"facebook.net", "hotjar.com", "scorecardresearch.com", "clarity.ms",
		"quantserve.com", "taboola.com", "outbrain.com"
	};
	private PermissionRequest pendingWebPermissionRequest;
	private String pendingGeolocationOrigin;
	private GeolocationPermissions.Callback pendingGeolocationCallback;
	
	private LinearLayout linear1;
	private LinearLayout linear_c;
	private LinearLayout linear82;
	private LinearLayout tela1;
	private LinearLayout tela2;
	private LinearLayout tela3;
	private TextView text_icon;
	private LinearLayout linear_barra_pesquisa;
	private EditText edittext1;
	private LinearLayout linear20;
	private LinearLayout linear_tela2_base;
	private LinearLayout linear_b;
	private LinearLayout vazio;
	private WebView webview;
	private TextView text_barra;
	private ImageButton button_site_info;
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
	private ImageButton button_load_stop;
	private UnsplashWallpaperManager wallpaperManager;
	private UnsplashWallpaperManager.Callback wallpaperCallback;
	private float pullStartY;
	private float pullStartX;
	private float pullDistance;
	private boolean pullTracking;
	private boolean pullConsumed;
	private SpringAnimation pullRotationSpring;
	private VelocityTracker pullVelocityTracker;
	private MaterialSwitch checkbox_javascript2;
	private TextView text5;
	private TextView b1;
	private TextView b2;
	
	private Intent intent = new Intent();
	private SharedPreferences Dados;
	private AlertDialog.Builder Limpar_Dialog;
	private TimerTask time;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		startDiagnostics();
		initialize(_savedInstanceState);
		initializeLogic();
		addDiagnosticMenuAction();
		showPendingCrashReport();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		linear_c = findViewById(R.id.linear_c);
		linear82 = findViewById(R.id.linear82);
		tela1 = findViewById(R.id.tela1);
		tela2 = findViewById(R.id.tela2);
		tela3 = findViewById(R.id.tela3);
		text_icon = findViewById(R.id.text_icon);
		linear_barra_pesquisa = findViewById(R.id.linear_barra_pesquisa);
		edittext1 = findViewById(R.id.edittext1);
		linear20 = findViewById(R.id.linear20);
		linear_tela2_base = findViewById(R.id.linear_tela2_base);
		linear_b = findViewById(R.id.linear_b);
		vazio = findViewById(R.id.vazio);
		webview = findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setSupportZoom(true);
		text_barra = findViewById(R.id.text_barra);
		button_site_info = findViewById(R.id.button_site_info);
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
		button_load_stop = findViewById(R.id.button_load_stop);
		checkbox_javascript2 = findViewById(R.id.checkbox_javascript2);
		text5 = findViewById(R.id.text5);
		b1 = findViewById(R.id.b1);
		b2 = findViewById(R.id.b2);
		Dados = getSharedPreferences("Dados", Activity.MODE_PRIVATE);

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
		if (button_load_stop != null) {
			button_load_stop.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (pullRotationSpring != null) pullRotationSpring.cancel();
					button_load_stop.setRotation(0f);
					if (pageLoading) {
						webview.stopLoading();
						finishPageProgress();
					} else if (webview.getUrl() != null && !webview.getUrl().equals("about:blank")) {
						webview.reload();
					} else {
						openSearchScreen();
					}
				}
			});
		}

		Limpar_Dialog = new AlertDialog.Builder(this);
		
		linear_barra_pesquisa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				openSearchScreen();
			}
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
		
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				logDiagnostic("Navegação iniciada: " + safeHostForDiagnostics(url));
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				logDiagnostic("Navegação concluída: " + safeHostForDiagnostics(url));
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
				logDiagnostic("Erro de carregamento WebView: código " + errorCode
					+ "; host=" + safeHostForDiagnostics(failingUrl));
				super.onReceivedError(view, errorCode, description, failingUrl);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler,
				android.net.http.SslError error) {
				logDiagnostic("Erro de certificado TLS: " + (error == null ? "desconhecido"
					: error.getPrimaryError()) + "; host="
					+ safeHostForDiagnostics(error == null ? null : error.getUrl()));
				handler.cancel();
			}
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
		
		linear_JavaScript.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				
			}
		});
		
		linear_limpar_dados2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Limpar_Dialog.setTitle("Limpar Dados de Navegação");
				Limpar_Dialog.setMessage("Você deseja limpar todos os dados de navegação?");
				Limpar_Dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						webview.clearCache(true);
						webview.clearHistory();
					}
				});
				Limpar_Dialog.setNegativeButton("NÃO", null);
				
				// Cria e exibe
				final AlertDialog d = Limpar_Dialog.create();
				d.show();
				
				// Customização do Fundo (Cor 34495E e Raio 20)
				android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
				gd.setColor(0xFF34495E);
				gd.setCornerRadius(20);
				d.getWindow().setBackgroundDrawable(gd);
				
				// Escurecer fundo da tela (Dim)
				d.getWindow().setDimAmount(0.7f);
				
				// Cores dos Botões (Cor 9E9E9E)
				d.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(0xFF9E9E9E);
				d.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(0xFF9E9E9E);
				
				// Ajuste da cor do Título (Branco)
				int titleId = getResources().getIdentifier("alertTitle", "id", "android");
				if (titleId > 0) {
					android.widget.TextView tvTitle = (android.widget.TextView) d.findViewById(titleId);
					if (tvTitle != null) {
						tvTitle.setTextColor(android.graphics.Color.WHITE);
					}
				}
				
				// Ajuste da cor da Mensagem (Branco)
				android.widget.TextView tvMsg = (android.widget.TextView) d.findViewById(android.R.id.message);
				if (tvMsg != null) {
					tvMsg.setTextColor(android.graphics.Color.WHITE);
				}
				
			}
		});
		
		linear_copiar_URL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				SketchwareUtil.showMessage(getApplicationContext(), "Copiado para a área de transferência");
				((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", webview.getUrl()));
			}
		});
		
		linear_tradutor_google.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				String url = webview.getUrl();
				if (url != null) {
					webview.loadUrl("https://translate.google.com/translate?sl=en&tl=pt&hl=pt-BR&u=" + url.replace("https://", "") + "&client=webapp");
					SketchwareUtil.showMessage(getApplicationContext(), "Traduzindo");
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), "Nenhuma URL encontrada para traduzir");
				}
				
			}
		});
		
		linear_modo_leitura.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				toque++;
				if ((toque % 2) == 1) {
					linear_modo_leitura.setAlpha((float)(0.5d));
					webview.setWebViewClient(new WebViewClient(){
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				WebResourceResponse blocked = blockInsecurePostFromSecurePage(view, request);
				if (blocked != null) return blocked;
				if (shouldBlockThirdPartyTracker(view, request)) {
					return new WebResourceResponse("text/plain", "UTF-8", 204, "Blocked",
						new java.util.HashMap<String, String>(), new java.io.ByteArrayInputStream(new byte[0]));
				}
				return super.shouldInterceptRequest(view, request);
			}

						@Override
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							if (url == null || !url.regionMatches(true, 0, "http://", 0, 7)) return false;
						showInsecureConnectionDialog(view, url);
						return true;
					}
						@Override
						public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
							handler.cancel();
						}

						@Override
						public void onPageFinished(WebView v, String url){
							String js =
							"javascript:(function(){"
							
							// container principal
							+ "var wrap=document.createElement('div');"
							+ "wrap.style.maxWidth='700px';"
							+ "wrap.style.margin='20px auto';"
							+ "wrap.style.padding='20px';"
							+ "wrap.style.fontFamily='sans-serif';"
							+ "wrap.style.fontSize='21px';"
							+ "wrap.style.lineHeight='1.7';"
							+ "wrap.style.color='#ECF0F1';"
							+ "wrap.style.background='#2C3E50';"
							+ "wrap.style.borderRadius='10px';"
							
							// elementos permitidos
							+ "var els=document.querySelectorAll('p,h1,h2,h3,img');"
							+ "for(var i=0;i<els.length;i++){"
							+ "   var el=els[i].cloneNode(true);"
							
							// imagens
							+ "   if(el.tagName==='IMG'){"
							+ "       var w=els[i].naturalWidth || els[i].width;"
							+ "       var h=els[i].naturalHeight || els[i].height;"
							
							// remove imagens pequenas (ícones inúteis)
							+ "       if(w<=80 || h<=80){ continue; }"
							
							// estilizar imagens
							+ "       el.style.display='block';"
							+ "       el.style.margin='25px auto';"
							+ "       el.style.maxWidth='100%';"
							+ "       el.style.height='auto';"
							+ "       el.style.borderRadius='10px';"
							+ "       el.style.boxShadow='0 0 15px rgba(0,0,0,0.4)';"
							+ "   }"
							
							// adiciona ao container
							+ "   wrap.appendChild(el);"
							+ "}"
							
							// limpar a página
							+ "document.body.innerHTML='';"
							+ "document.body.style.background='#2C3E50';"
							+ "document.body.appendChild(wrap);"
							
							+ "})();";
							
							if(android.os.Build.VERSION.SDK_INT>=19){
								v.evaluateJavascript(js, null);
							} else {
								v.loadUrl(js);
							}
						}
					});
				} else {
					linear_modo_leitura.setAlpha((float)(1.0d));
					webview.setWebViewClient(new WebViewClient(){
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
				WebResourceResponse blocked = blockInsecurePostFromSecurePage(view, request);
				if (blocked != null) return blocked;
				if (shouldBlockThirdPartyTracker(view, request)) {
					return new WebResourceResponse("text/plain", "UTF-8", 204, "Blocked",
						new java.util.HashMap<String, String>(), new java.io.ByteArrayInputStream(new byte[0]));
				}
				return super.shouldInterceptRequest(view, request);
			}

						@Override
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							if (url == null || !url.regionMatches(true, 0, "http://", 0, 7)) return false;
						showInsecureConnectionDialog(view, url);
						return true;
					}
						@Override
						public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
							handler.cancel();
						}

						@Override
						public void onPageFinished(WebView v, String url){
							String js =
							"javascript:(function(){"
							// resetando estilo do body
							+ "document.body.style.background='';"
							+ "document.body.style.color='';"
							+ "document.body.style.margin='';"
							+ "document.body.style.maxWidth='';"
							+ "document.body.style.padding='';"
							// mostrando de volta elementos que o modo leitura escondeu
							+ "document.querySelectorAll('header, nav, footer, aside').forEach(e => e.style.display='');"
							+ "})();";
							
							if(android.os.Build.VERSION.SDK_INT >= 19){
								v.evaluateJavascript(js, null);
							} else {
								v.loadUrl(js);
							}
						}
					});
				}
				webview.loadUrl(webview.getUrl());
			}
		});
		
		linear_abrir_no_navegador.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(webview.getUrl()));
				startActivity(intent);
			}
		});
		
		checkbox_brilho.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("brilho", "ativado").apply();
					webview.setAlpha((float)(0.5d));
				} else {
					Dados.edit().putString("brilho", "desativado").apply();
					webview.setAlpha((float)(1.0d));
				}
			}
		});
		
		checkbox_javascript2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2) {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					Dados.edit().putString("java", "ativado").apply();
					webview.getSettings().setJavaScriptEnabled(true); 
				} else {
					webview.getSettings().setJavaScriptEnabled(false); 
					Dados.edit().putString("java", "desativado").apply();
				}
			}
		});
		
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				tela1.setVisibility(View.GONE);
				tela2.setVisibility(View.VISIBLE);
				tela3.setVisibility(View.GONE);
			}
		});
		
		b2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				tela1.setVisibility(View.GONE);
				tela2.setVisibility(View.GONE);
				tela3.setVisibility(View.VISIBLE);
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
			showInsecureConnectionDialog(webview, destination);
		} else {
			loadUrlWithPrivacyHeaders(webview, destination);
		}
		tela1.setVisibility(View.GONE);
		tela2.setVisibility(View.VISIBLE);
		tela3.setVisibility(View.GONE);
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
		if (tela1 != null) tela1.setVisibility(View.VISIBLE);
		if (tela2 != null) tela2.setVisibility(View.GONE);
		if (tela3 != null) tela3.setVisibility(View.GONE);
		if (edittext1 == null) return;

		edittext1.setShowSoftInputOnFocus(true);
		edittext1.requestFocus();
		getWindow().setSoftInputMode(
			android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
				| android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		);
		edittext1.post(new Runnable() {
			@Override
			public void run() {
				if (isFinishing() || !edittext1.isShown()) return;
				android.view.inputmethod.InputMethodManager keyboard =
					(android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (keyboard != null) {
					keyboard.showSoftInput(edittext1, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
				}
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
		new AlertDialog.Builder(this)
			.setTitle("Falha detectada no Snav")
			.setMessage("O Snav detectou que a execução anterior terminou inesperadamente. Você pode copiar o diagnóstico para ajudar a identificar a causa.")
			.setPositiveButton("Copiar diagnóstico", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (copyDiagnosticReport()) marker.delete();
				}
			})
			.setNegativeButton("Depois", null)
			.show();
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
	protected void onResume() {
		super.onResume();
		logDiagnostic("Ciclo de vida: onResume");
	}

	@Override
	protected void onPause() {
		logDiagnostic("Ciclo de vida: onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		logDiagnostic("Ciclo de vida: onDestroy; isFinishing=" + isFinishing());
		wallpaperCallback = null;
		if (pullRotationSpring != null) pullRotationSpring.cancel();
		if (pullVelocityTracker != null) {
			pullVelocityTracker.recycle();
			pullVelocityTracker = null;
		}
		super.onDestroy();
	}

	private final Handler progressHandler = new Handler(Looper.getMainLooper());
	private ObjectAnimator progressAnimator;
	private Runnable showProgressRunnable;
	private boolean pageLoading = false;

	private void beginPageProgress() {
		pageLoading = true;
		setLoadStopIcon(true);
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
		setLoadStopIcon(false);
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

	private int siteInfoDp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void addSiteInfoRow(LinearLayout parent, String title, String value) {
		TextView heading = new TextView(this);
		heading.setText(title);
		heading.setTextColor(Color.parseColor("#DCE1E6"));
		heading.setTextSize(12);
		parent.addView(heading);

		TextView detail = new TextView(this);
		detail.setText(value);
		detail.setTextColor(Color.WHITE);
		detail.setTextSize(14);
		detail.setTextIsSelectable(true);
		detail.setPadding(0, siteInfoDp(3), 0, siteInfoDp(12));
		parent.addView(detail);
	}

	private void showSiteInfoBottomSheet() {
		final String currentUrl = webview.getUrl();
		final Uri pageUri = currentUrl == null ? null : Uri.parse(currentUrl);
		final String scheme = pageUri == null ? null : pageUri.getScheme();
		final String host = pageUri == null ? null : pageUri.getHost();
		final String origin;
		if (scheme != null && host != null) {
			int port = pageUri.getPort();
			origin = scheme + "://" + host + (port >= 0 ? ":" + port : "");
		} else {
			origin = null;
		}

		final Dialog sheet = new Dialog(this);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(siteInfoDp(24), siteInfoDp(22), siteInfoDp(24), siteInfoDp(28));
		GradientDrawable surface = new GradientDrawable();
		surface.setColor(Color.parseColor("#34495E"));
		surface.setCornerRadii(new float[] { siteInfoDp(24), siteInfoDp(24), siteInfoDp(24), siteInfoDp(24), 0, 0, 0, 0 });
		content.setBackground(surface);

		TextView title = new TextView(this);
		title.setText("Informações do site");
		title.setTextColor(Color.WHITE);
		title.setTextSize(20);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		title.setPadding(0, 0, 0, siteInfoDp(18));
		content.addView(title);

		addSiteInfoRow(content, "ENDEREÇO COMPLETO", currentUrl == null ? "Nenhum endereço aberto" : currentUrl);
		String connection;
		if ("https".equalsIgnoreCase(scheme)) {
			connection = "HTTPS — conexão criptografada";
		} else if ("http".equalsIgnoreCase(scheme)) {
			connection = "HTTP — conexão não criptografada";
		} else {
			connection = "Não disponível";
		}
		addSiteInfoRow(content, "CONEXÃO", connection);
		addSiteInfoRow(content, "PERMISSÕES DO SITE", "Câmera e microfone dependem também das permissões Android do aplicativo. A localização pode ser limpa para esta origem. O WebView não oferece API pública para revogar câmera/microfone individualmente por domínio; use as configurações Android para revogação global.");
		addSiteInfoRow(content, "COOKIES E DADOS", "O Snav remove o DOM Storage da origem e tenta expirar cookies visíveis no caminho raiz. O WebView não expõe uma API pública para apagar todos os cookies de uma única origem com garantia de escopo.");

		TextView clearData = new TextView(this);
		clearData.setText("Limpar armazenamento local");
		clearData.setTextColor(Color.WHITE);
		clearData.setTextSize(14);
		clearData.setGravity(Gravity.CENTER);
		clearData.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
		clearData.setMinHeight(siteInfoDp(48));
		clearData.setPadding(siteInfoDp(16), siteInfoDp(10), siteInfoDp(16), siteInfoDp(10));
		GradientDrawable actionBg = new GradientDrawable();
		actionBg.setColor(Color.parseColor("#E67E22"));
		actionBg.setCornerRadius(siteInfoDp(24));
		clearData.setBackgroundResource(R.drawable.snav_accent_ripple);
		clearData.setClickable(true);
		clearData.setFocusable(true);
		content.addView(clearData, new LinearLayout.LayoutParams(-1, -2));
		clearData.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (origin == null) {
					Toast.makeText(MainActivity.this, "Nenhum domínio disponível", Toast.LENGTH_SHORT).show();
					return;
				}
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("Limpar dados do site")
					.setMessage("Remover o armazenamento local e tentar apagar os cookies visíveis de " + host + "? Cookies com caminhos ou domínios específicos podem exigir limpeza adicional.")
					.setNegativeButton("Cancelar", null)
					.setPositiveButton("Limpar", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							WebStorage.getInstance().deleteOrigin(origin);
							clearCookiesForOrigin(currentUrl);
							Toast.makeText(MainActivity.this, "Limpeza solicitada para este domínio", Toast.LENGTH_SHORT).show();
						}
					}).show();
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
		LinearLayout.LayoutParams permissionParams = new LinearLayout.LayoutParams(-1, -2);
		permissionParams.topMargin = siteInfoDp(8);
		content.addView(managePermissions, permissionParams);
		managePermissions.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (origin != null) {
					GeolocationPermissions.getInstance().clear(origin);
				}
				Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
					Uri.parse("package:" + getPackageName()));
				try {
					startActivity(settings);
				} catch (ActivityNotFoundException error) {
					Toast.makeText(MainActivity.this, "Não foi possível abrir as configurações do aplicativo", Toast.LENGTH_SHORT).show();
				}
			}
		});

		addSiteInfoRow(content, "CONFIGURAÇÕES DO SITE", "JavaScript: " + (webview.getSettings().getJavaScriptEnabled() ? "ativado" : "desativado") + ". Esta configuração é global para o navegador, não específica deste domínio.");

		ScrollView scroll = new ScrollView(this);
		scroll.setFillViewport(true);
		scroll.setClipToPadding(false);
		scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
		sheet.setContentView(scroll);
		sheet.setCancelable(true);
		Window window = sheet.getWindow();
		if (window != null) {
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			WindowManager.LayoutParams params = window.getAttributes();
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.gravity = Gravity.BOTTOM;
			params.dimAmount = 0.38f;
			window.setAttributes(params);
		}
		sheet.show();
		if (sheet.getWindow() != null) {
			sheet.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		}
	}



	private void grantWebPermissionWhenAndroidAllows(PermissionRequest request, String[] resources) {
		if (request == null || isFinishing()) return;
		java.util.ArrayList<String> required = new java.util.ArrayList<String>();
		for (String resource : resources) {
			if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
				required.add(android.Manifest.permission.CAMERA);
			} else if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
				required.add(android.Manifest.permission.RECORD_AUDIO);
			} else {
				request.deny();
				return;
			}
		}
		if (Build.VERSION.SDK_INT >= 23) {
			java.util.ArrayList<String> missing = new java.util.ArrayList<String>();
			for (String permission : required) {
				if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) missing.add(permission);
			}
			if (!missing.isEmpty()) {
				pendingWebPermissionRequest = request;
				requestPermissions(missing.toArray(new String[missing.size()]), 4101);
				return;
			}
		}
		request.grant(resources);
	}

	private void grantGeolocationWhenAndroidAllows(String origin, GeolocationPermissions.Callback callback) {
		if (Build.VERSION.SDK_INT >= 23
			&& checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
			&& checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
			pendingGeolocationOrigin = origin;
			pendingGeolocationCallback = callback;
			requestPermissions(new String[] {
				android.Manifest.permission.ACCESS_FINE_LOCATION,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			}, 4102);
			return;
		}
		callback.invoke(origin, true, false);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 4101) {
			PermissionRequest pending = pendingWebPermissionRequest;
			pendingWebPermissionRequest = null;
			if (pending == null) return;
			boolean granted = grantResults.length > 0;
			for (int result : grantResults) granted = granted && result == android.content.pm.PackageManager.PERMISSION_GRANTED;
			if (granted) pending.grant(pending.getResources()); else pending.deny();
		} else if (requestCode == 4102) {
			GeolocationPermissions.Callback callback = pendingGeolocationCallback;
			String origin = pendingGeolocationOrigin;
			pendingGeolocationCallback = null;
			pendingGeolocationOrigin = null;
			if (callback == null) return;
			boolean granted = Build.VERSION.SDK_INT < 23
				|| checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
			callback.invoke(origin, granted, false);
		}
	}

	private void addThirdPartyCookiePreference(boolean enabled) {
		if (linear33 == null || android.os.Build.VERSION.SDK_INT < 21
			|| linear33.findViewWithTag("snav_third_party_cookie_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_third_party_cookie_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Cookies de terceiros");
		toggle.setTextColor(getResources().getColor(R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Bloqueados por padrão; permitir pode aumentar o rastreamento entre sites.");
		summary.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		summary.setTextSize(12);
		summary.setPadding(dp(40), 0, dp(8), 0);
		row.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
		row.addView(summary, new LinearLayout.LayoutParams(-1, -2));
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton button, boolean checked) {
				Dados.edit().putBoolean("third_party_cookies", checked).apply();
				CookieManager.getInstance().setAcceptThirdPartyCookies(webview, checked);
				com.google.android.material.snackbar.Snackbar.make(linear33,
					checked ? "Cookies de terceiros permitidos" : "Cookies de terceiros bloqueados",
					com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
			}
		});
		int insertAt = Math.max(0, linear33.getChildCount() - 1);
		linear33.addView(row, insertAt, new LinearLayout.LayoutParams(-1, -2));
	}

	private void addTrackerBlockingPreference(boolean enabled) {
		if (linear33 == null || linear33.findViewWithTag("snav_tracker_preference") != null) return;
		LinearLayout row = new LinearLayout(this);
		row.setTag("snav_tracker_preference");
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(dp(8), dp(8), dp(8), dp(8));
		MaterialSwitch toggle = new MaterialSwitch(this);
		toggle.setText("Bloquear rastreadores conhecidos");
		toggle.setTextColor(getResources().getColor(R.color.snav_on_surface));
		toggle.setTextSize(14);
		toggle.setChecked(enabled);
		TextView summary = new TextView(this);
		summary.setText("Filtro local e limitado a domínios conhecidos de terceiros; não bloqueia todo rastreamento.");
		summary.setTextColor(getResources().getColor(R.color.snav_on_surface_variant));
		summary.setTextSize(12);
		summary.setPadding(dp(40), 0, dp(8), 0);
		row.addView(toggle, new LinearLayout.LayoutParams(-1, dp(48)));
		row.addView(summary, new LinearLayout.LayoutParams(-1, -2));
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton button, boolean checked) {
				Dados.edit().putBoolean("tracker_blocking", checked).apply();
				com.google.android.material.snackbar.Snackbar.make(linear33,
					checked ? "Filtro de rastreadores ativado" : "Filtro de rastreadores desativado",
					com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
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

	private void loadUrlWithPrivacyHeaders(WebView view, String url) {
		if (view == null || url == null) return;
		if (Dados.getBoolean("do_not_track", false)) {
			java.util.HashMap<String, String> headers = new java.util.HashMap<String, String>();
			headers.put("DNT", "1");
			view.loadUrl(url, headers);
		} else {
			view.loadUrl(url);
		}
	}

	private void clearCookiesForOrigin(String url) {
		if (url == null) return;
		CookieManager manager = CookieManager.getInstance();
		String cookies = manager.getCookie(url);
		if (cookies == null || cookies.trim().isEmpty()) return;
		String[] pairs = cookies.split(";");
		for (String pair : pairs) {
			int separator = pair.indexOf('=');
			if (separator <= 0) continue;
			String name = pair.substring(0, separator).trim();
			if (!name.matches("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) continue;
			manager.setCookie(url, name + "=; Path=/; Max-Age=0; SameSite=Lax");
		}

	}

	private boolean shouldBlockThirdPartyTracker(WebView view, WebResourceRequest request) {
		if (view == null || request == null || request.isForMainFrame()) return false;
		return shouldBlockThirdPartyTracker(request);
	}

	private boolean shouldBlockThirdPartyTracker(WebResourceRequest request) {
		if (!Dados.getBoolean("tracker_blocking", true) || request == null) return false;
		Uri resource = request.getUrl();
		String referrer = request.getRequestHeaders().get("Referer");
		if (referrer == null || referrer.trim().isEmpty()) return false;
		Uri page = Uri.parse(referrer);
		String resourceHost = resource == null ? null : resource.getHost();
		String pageHost = page.getHost();
		if (resourceHost == null || pageHost == null || resourceHost.equalsIgnoreCase(pageHost)) return false;
		if (isSameSiteHost(resourceHost, pageHost)) return false;
		for (String domain : TRACKER_DOMAINS) {
			if (resourceHost.equalsIgnoreCase(domain)
				|| resourceHost.toLowerCase(java.util.Locale.US).endsWith("." + domain)) return true;
		}
		return false;
	}

	private boolean isSameSiteHost(String first, String second) {
		String a = first.toLowerCase(java.util.Locale.US);
		String b = second.toLowerCase(java.util.Locale.US);
		return a.endsWith("." + b) || b.endsWith("." + a);
	}

	private int dp(float value) {
		return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
	}

	private WebResourceResponse blockInsecurePostFromSecurePage(final WebView view, WebResourceRequest request) {
		if (request == null || !request.isForMainFrame()
			|| !"POST".equalsIgnoreCase(request.getMethod())) return null;
		Uri target = request.getUrl();
		if (target == null || !"http".equalsIgnoreCase(target.getScheme())) return null;
		String current = view == null ? null : view.getUrl();
		if (current == null || !current.regionMatches(true, 0, "https://", 0, 8)) return null;
		if (view != null) {
			view.post(new Runnable() {
				@Override public void run() {
					Toast.makeText(MainActivity.this,
						"Envio de formulário HTTP bloqueado para proteger seus dados.",
						Toast.LENGTH_LONG).show();
				}
			});
		}
		return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked",
			new java.util.HashMap<String, String>(), new java.io.ByteArrayInputStream(new byte[0]));
	}

	private void showInsecureConnectionDialog(final WebView view, final String url) {
		final Uri parsed = Uri.parse(url);
		final String host = parsed.getHost() == null ? "este endereço" : parsed.getHost();
		final String secureUrl = parsed.buildUpon().scheme("https").build().toString();
		new AlertDialog.Builder(this)
			.setTitle("HTTP bloqueado")
			.setMessage("O Snav não envia dados por HTTP. A conexão com " + host + " não é criptografada. Deseja tentar abrir a versão HTTPS? Se o site não oferecer HTTPS, a navegação será bloqueada.")
			.setPositiveButton("Tentar HTTPS", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					loadUrlWithPrivacyHeaders(view, secureUrl);
				}
			})
			.setNegativeButton("Cancelar", null)
			.show();
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
		private final Paint shadePaint = new Paint();
		UnsplashWallpaperDrawable(Bitmap image) {
			bitmap = image;
			shadePaint.setColor(0x66000000);
		}
		@Override public void draw(Canvas canvas) {
			if (bitmap == null || bitmap.isRecycled()) return;
			android.graphics.Rect bounds = getBounds();
			float scale = Math.max((float) bounds.width() / bitmap.getWidth(),
				(float) bounds.height() / bitmap.getHeight());
			float width = bitmap.getWidth() * scale;
			float height = bitmap.getHeight() * scale;
			float left = bounds.left + (bounds.width() - width) / 2f;
			float top = bounds.top + (bounds.height() - height) / 2f;
			canvas.drawBitmap(bitmap, null, new android.graphics.RectF(left, top, left + width, top + height), imagePaint);
			canvas.drawRect(bounds, shadePaint);
		}
		@Override public void setAlpha(int alpha) { shadePaint.setAlpha(alpha); invalidateSelf(); }
		@Override public void setColorFilter(android.graphics.ColorFilter filter) { imagePaint.setColorFilter(filter); invalidateSelf(); }
		@Override public int getOpacity() { return android.graphics.PixelFormat.OPAQUE; }
	}

	private void setupPullToRefresh() {
		if (webview == null || button_load_stop == null) return;
		webview.setOverScrollMode(View.OVER_SCROLL_NEVER);
		webview.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, android.view.MotionEvent event) {
				if (event.getActionMasked() != android.view.MotionEvent.ACTION_DOWN
					&& pullVelocityTracker != null) pullVelocityTracker.addMovement(event);
				switch (event.getActionMasked()) {
					case android.view.MotionEvent.ACTION_DOWN:
						if (pullVelocityTracker != null) pullVelocityTracker.recycle();
						pullVelocityTracker = VelocityTracker.obtain();
						pullVelocityTracker.addMovement(event);
						pullStartY = event.getY();
						pullStartX = event.getX();
						pullDistance = 0f;
						pullTracking = webview.getScrollY() <= 0 && !pageLoading;
						pullConsumed = false;
						if (pullRotationSpring != null) pullRotationSpring.cancel();
						button_load_stop.setRotation(0f);
						return false;
					case android.view.MotionEvent.ACTION_MOVE:
						if (!pullTracking || webview.getScrollY() > 0) return false;
						float horizontalDistance = Math.abs(event.getX() - pullStartX);
						pullDistance = event.getY() - pullStartY;
						if (horizontalDistance > pullDistance) {
							pullTracking = false;
							pullConsumed = false;
							button_load_stop.setRotation(0f);
							return false;
						}
						if (pullDistance <= 0f) return false;
						button_load_stop.setRotation(Math.min(180f, pullDistance * 0.8f));
						if (pullDistance > getDip(12)) pullConsumed = true;
						return pullConsumed;
					case android.view.MotionEvent.ACTION_UP:
					case android.view.MotionEvent.ACTION_CANCEL:
						boolean refresh = event.getActionMasked() == android.view.MotionEvent.ACTION_UP
							&& pullTracking && pullConsumed && pullDistance >= getDip(72);
						float rotation = button_load_stop.getRotation();
						float rotationVelocity = 0f;
						if (pullVelocityTracker != null) {
							pullVelocityTracker.computeCurrentVelocity(1000);
							rotationVelocity = Math.max(-3600f, Math.min(3600f,
								pullVelocityTracker.getYVelocity() * 0.8f));
							pullVelocityTracker.recycle();
							pullVelocityTracker = null;
						}
						pullTracking = false;
						pullConsumed = false;
						if (event.getActionMasked() == android.view.MotionEvent.ACTION_CANCEL) rotationVelocity = 0f;
						if (rotation > 0f) animatePullRelease(rotationVelocity);
						if (refresh && webview.getUrl() != null) {
							webview.reload();
							return true;
						}
						return false;
				}
				return false;
			}
		});
	}

	private void animatePullRelease(float initialVelocity) {
		if (button_load_stop == null) return;
		if (pullRotationSpring != null) pullRotationSpring.cancel();
		if (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) {
			button_load_stop.setRotation(0f);
			return;
		}

		SpringForce spatialSpring = MotionUtils.resolveThemeSpringForce(
			this,
			com.google.android.material.R.attr.motionSpringFastSpatial,
			com.google.android.material.R.style.Motion_Material3_Spring_Standard_Fast_Spatial);
		pullRotationSpring = new SpringAnimation(
			button_load_stop, DynamicAnimation.ROTATION, 0f);
		// Keep the animation's target position (0 degrees) and copy only the
		// Material spring parameters. Replacing its SpringForce directly leaves
		// the resolver's final position unset and can crash SpringAnimation.start().
		pullRotationSpring.getSpring()
			.setDampingRatio(spatialSpring.getDampingRatio())
			.setStiffness(spatialSpring.getStiffness());
		pullRotationSpring.setStartVelocity(initialVelocity);
		pullRotationSpring.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_ROTATION_DEGREES);
		pullRotationSpring.start();
	}


	private void setLoadStopIcon(boolean loading) {
		if (button_load_stop == null) return;
		button_load_stop.setImageResource(loading ? R.drawable.ms_stop : R.drawable.ms_refresh);
		button_load_stop.setContentDescription(loading ? "Interromper carregamento" : "Atualizar página");
	}

	private void initializeLogic() {
		_armazenamento();
		_open_sans();
		_interfaces();
		_download();
		_internet();
		_gone();
	}
	
	@Override
	public void onBackPressed() {
		if (webview.canGoBack()) {
			webview.goBack();
		} else {
			tela1.setVisibility(View.VISIBLE);
			tela2.setVisibility(View.GONE);
			tela3.setVisibility(View.GONE);
			toque++;
			if (toque == 1) {
				time = new TimerTask() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								toque = 0;
							}
						});
					}
				};
				_timer.schedule(time, (int)(2000));
			} else {
				finish();
			}
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
		//linear_c
		b1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
		b2.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/open_regular.ttf"), 0);
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
        geckoView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        geckoEngine = new GeckoEngineController(this, geckoView,
            new GeckoEngineController.Listener() {
                @Override public void onPageStarted(String uri) {
                    logDiagnostic("Navegação Gecko iniciada: " + safeHostForDiagnostics(uri));
                    beginPageProgress();
                    vazio.setVisibility(View.GONE);
                    setLoadStopIcon(true);
                }
                @Override public void onPageStopped(boolean success) {
                    finishPageProgress();
                    setLoadStopIcon(false);
                    String uri = geckoEngine.getCurrentUri();
                    vazio.setVisibility(uri == null || uri.isEmpty() || "about:blank".equals(uri) ? View.VISIBLE : View.GONE);
                    if (!success) logDiagnostic("Falha de carregamento Gecko: " + safeHostForDiagnostics(uri));
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
                    Uri parsed = Uri.parse(uri);
                    text_barra.setText(parsed.getHost() == null ? uri : parsed.getHost());
                }
                @Override public void onTitleChanged(String title) {
                    if (title != null && !title.trim().isEmpty()) text_barra.setText(title);
                }
                @Override public void onInsecureNavigationBlocked(String uri, String source) {
                    showInsecureConnectionDialog(uri);
                }
                @Override public void onExternalNavigationRequested(String uri) {
                    openExternalUri(uri);
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
                    finishPageProgress();
                    setLoadStopIcon(false);
                    Toast.makeText(MainActivity.this, "A página encontrou um problema. Tente recarregar.", Toast.LENGTH_LONG).show();
                }
                @Override public void onScrollChanged(int scrollY) { geckoScrollY = Math.max(0, scrollY); }
                @Override public void onHistoryVisited(String uri) { saveHistoryVisit(uri); }
            },
            javaScriptEnabled,
            Dados.getBoolean("tracker_blocking", true));
        installGeckoPermissionDelegate();
        setupPullToRefresh();
    }
	
	
	public void _armazenamento() {
		//PARA O ARMAZENAMENTO DE DADOS
		//brilho da tela
		if (Dados.getString("brilho", "").equals("ativado")) {
			checkbox_brilho.setChecked(true);
			webview.setAlpha((float)(0.5d));
		} else {
			
		}
		if (Dados.getString("brilho", "").equals("desativado")) {
			checkbox_brilho.setChecked(false);
			webview.setAlpha((float)(1.0d));
		} else {
			
		}
		//javascript
		if (Dados.getString("java", "").equals("ativado")) {
			checkbox_javascript2.setChecked(true);
			webview.getSettings().setJavaScriptEnabled(true); 
		} else {
			
		}
		if (Dados.getString("java", "").equals("desativado")) {
			checkbox_javascript2.setChecked(false);
			webview.getSettings().setJavaScriptEnabled(false); 
		} else {
			
		}
		//vazio
		if (Dados.getString("vazio", "").equals("desativado")) {
			webview.setWebViewClient(new android.webkit.WebViewClient() {
				@Override
				public void onPageFinished(android.webkit.WebView view, String url) {
					super.onPageFinished(view, url);
					
					String u = view.getUrl();
					
					if (u == null || u.equals("") || u.equals("about:blank")) {
						vazio.setVisibility(android.view.View.VISIBLE);
					} else {
						vazio.setVisibility(android.view.View.GONE);
					}
				}
			});
		} else {
			
		}
	}
	
	
	public void _criação() {
		
	}
	
	
	public void _download() {
		webview.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
				Uri downloadUri = Uri.parse(url == null ? "" : url);
				String downloadScheme = downloadUri.getScheme();
				if (downloadUri.getHost() == null || downloadUri.getUserInfo() != null
					|| !"https".equalsIgnoreCase(downloadScheme)) {
					Toast.makeText(MainActivity.this, "Download bloqueado: somente endereços HTTPS são permitidos.", Toast.LENGTH_LONG).show();
					return;
				}
				String suggestedName = URLUtil.guessFileName(url, contentDisposition, mimetype);
				final String fileName = (suggestedName == null ? "download" : suggestedName)
					.replace('/', '_').replace(':', '_');
				
				// Verifica permissão no Sketchware/Android
				if (android.os.Build.VERSION.SDK_INT >= 23 && android.os.Build.VERSION.SDK_INT <= 28) {
					if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED) {
						requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Download");
				builder.setMessage("Deseja baixar o arquivo " + fileName + "?");
				builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (android.os.Build.VERSION.SDK_INT >= 23 && android.os.Build.VERSION.SDK_INT <= 28
							&& checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
							!= android.content.pm.PackageManager.PERMISSION_GRANTED) {
							Toast.makeText(MainActivity.this, "Permissão de armazenamento negada. Download cancelado.", Toast.LENGTH_LONG).show();
							return;
						}
						try {
							DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
							request.setTitle(fileName);
							request.setDescription("Baixando...");
							request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
							
							// Adiciona UserAgent e Cookies para evitar erros de 403 Forbidden
								String cookie = android.webkit.CookieManager.getInstance().getCookie(url);
							if (cookie != null && !cookie.trim().isEmpty()) {
								request.addRequestHeader("Cookie", cookie);
							}
							if (userAgent != null && !userAgent.trim().isEmpty()) {
								request.addRequestHeader("User-Agent", userAgent);
							}
							
							DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
							if (dm == null) throw new IllegalStateException("Gerenciador de downloads indisponível");
							dm.enqueue(request);
							android.widget.Toast.makeText(getApplicationContext(), "Download iniciado...", android.widget.Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							logDiagnostic("Falha ao iniciar download: " + e.getClass().getSimpleName());
							android.widget.Toast.makeText(getApplicationContext(), "Não foi possível iniciar o download", android.widget.Toast.LENGTH_SHORT).show();
						}
					}
				});
				builder.setNegativeButton("Não", null);
				
				// Estilo Customizado (Bordas 20, Fundo 34495E, Botões 9E9E9E)
				final AlertDialog d = builder.create();
				d.show();
				
				android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
				gd.setColor(0xFF34495E);
				gd.setCornerRadius(20);
				d.getWindow().setBackgroundDrawable(gd);
				d.getWindow().setDimAmount(0.7f);
				
				d.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(0xFF9E9E9E);
				d.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(0xFF9E9E9E);
				
				// Cor do texto para contraste
				android.widget.TextView tvMsg = (android.widget.TextView) d.findViewById(android.R.id.message);
				if (tvMsg != null) tvMsg.setTextColor(android.graphics.Color.WHITE);
				
				int titleId = getResources().getIdentifier("alertTitle", "id", "android");
				if (titleId > 0) {
					android.widget.TextView tvTitle = (android.widget.TextView) d.findViewById(titleId);
					tvTitle.setTextColor(android.graphics.Color.WHITE);
				}
			}
		});
		
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
package uroz.cristina.smartwallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

/**
 */
  public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      boolean b = requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.splash);

      int DELAY_TIME = 1500;
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run() {
          Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
          SplashActivity.this.startActivity(mainIntent);
          SplashActivity.this.finish();
        }
      }, DELAY_TIME);
    }
  }


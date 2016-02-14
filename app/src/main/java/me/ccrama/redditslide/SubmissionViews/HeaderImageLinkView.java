package me.ccrama.redditslide.SubmissionViews;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dean.jraw.models.Submission;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 2/7/2016.
 */
public class HeaderImageLinkView extends RelativeLayout {
    private TextView title;
    private TextView info;
    private ImageView backdrop;

    public HeaderImageLinkView(Context context) {
        super(context);
        init();
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;

        Log.v(LogUtil.getTag(), "Ratio is " + ratio);
        double width = getWidth();
        return (width * ratio);

    }

    boolean done;

    public void setSubmission(final Submission submission, final boolean full) {
        backdrop.setImageResource(android.R.color.transparent); //reset the image view in case the placeholder is still visible
        doImageAndText(submission, full);

        if (!done && full) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!done) {
                        doImageAndText(submission, full);
                        done = true;
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                }
            });
        } else {
            done = true;
            doImageAndText(submission, full);
        }

    }

    public void doImageAndText(Submission submission, boolean full) {
        final ContentType.ImageType type = ContentType.getImageType(submission);

        setVisibility(View.VISIBLE);
        String url = "";
        boolean forceThumb = false;

        if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("width")) {

            int height = submission.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt();
            int width = submission.getDataNode().get("preview").get("images").get(0).get("source").get("width").asInt();

            if (full) {
                if (height < dpToPx(100) && type != ContentType.ImageType.SELF) {
                    forceThumb = true;
                } else if(SettingValues.cropImage){
                    backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(200)));
                } else{
                    double h = getHeightFromAspectRatio(height, width);
                    backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int)h ));
                }
            } else if (SettingValues.bigPicCropped) {
                if (height < dpToPx(200)) {
                    forceThumb = true;
                } else {
                    backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(200)));
                }
            } else if (height >= dpToPx(150) ) {
                backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getHeightFromAspectRatio(height, width)));
            } else {
                forceThumb = true;
            }

        }
        if (submission.isNsfw() && !SettingValues.NSFWPreviews) {

            setVisibility(View.GONE);
            if (!full || forceThumb) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                wrapArea.setVisibility(View.VISIBLE);
            }
            if (submission.isSelfPost()) thumbImage2.setVisibility(View.GONE);
            else {
                thumbImage2.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nsfw));

            }

        } else if (type != ContentType.ImageType.IMAGE && type != ContentType.ImageType.SELF && (submission.getThumbnailType() != Submission.ThumbnailType.URL)) {

            setVisibility(View.GONE);
            if (!full) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                wrapArea.setVisibility(View.VISIBLE);
            }

            thumbImage2.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.web));
        } else if (type == ContentType.ImageType.IMAGE) {

            url = submission.getUrl();
            if (!full && !SettingValues.bigPicEnabled || forceThumb) {

                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                setVisibility(View.GONE);

            } else {

                ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop);
                setVisibility(View.VISIBLE);
                if (!full) {
                    thumbImage2.setVisibility(View.GONE);
                } else {
                    wrapArea.setVisibility(View.GONE);
                }
            }
        } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) {

            url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
            if (!SettingValues.bigPicEnabled && !full || forceThumb) {

                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                setVisibility(View.GONE);

            } else {

                ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop);
                setVisibility(View.VISIBLE);
                if (!full) {
                    thumbImage2.setVisibility(View.GONE);
                } else {
                    wrapArea.setVisibility(View.GONE);
                }
            }
        } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

            if (!full) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                wrapArea.setVisibility(View.VISIBLE);
            }
            ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
            setVisibility(View.GONE);


        } else {

            if (!full) {
                thumbImage2.setVisibility(View.GONE);
            } else {
                wrapArea.setVisibility(View.GONE);
            }
            setVisibility(View.GONE);
        }

        if (full) {
            if (wrapArea.getVisibility() == View.VISIBLE) {
                title = secondTitle;
                info = secondSubTitle;
            } else {
                title = (TextView) findViewById(R.id.textimage);
                info = (TextView) findViewById(R.id.subtextimage);
            }
        } else {
            title = (TextView) findViewById(R.id.textimage);
            info = (TextView) findViewById(R.id.subtextimage);
        }


        title.setVisibility(View.VISIBLE);
        info.setVisibility(View.VISIBLE);

        switch (type) {
            case NSFW_IMAGE:
                title.setText(R.string.type_nsfw_img);
                break;

            case NSFW_GIF:
            case NSFW_GFY:
                title.setText(R.string.type_nsfw_gif);
                break;

            case REDDIT:
                title.setText(R.string.type_reddit);
                break;

            case LINK:
            case IMAGE_LINK:
                title.setText(R.string.type_link);
                break;

            case NSFW_LINK:
                title.setText(R.string.type_nsfw_link);

                break;
            case SELF:
                title.setVisibility(View.GONE);
                info.setVisibility(View.GONE);

                break;

            case ALBUM:
                title.setText(R.string.type_album);
                break;

            case IMAGE:
                if (submission.isNsfw() && !SettingValues.NSFWPreviews) {
                    title.setText(R.string.type_nsfw_img);

                } else {
                    title.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);
                }
                break;
            case IMGUR:
                title.setText(R.string.type_imgur);
                break;
            case GFY:
            case GIF:
            case NONE_GFY:
            case NONE_GIF:
                title.setText(R.string.type_gif);
                break;

            case NONE:
                title.setText(R.string.type_title_only);
                break;

            case NONE_IMAGE:
                title.setText(R.string.type_img);
                break;

            case VIDEO:
                title.setText(R.string.type_vid);
                break;

            case EMBEDDED:
                title.setText(R.string.type_emb);
                break;

            case NONE_URL:
                title.setText(R.string.type_link);
                break;

        }


        try {
            info.setText(getDomainName(submission.getUrl()));
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }


    private String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private ImageView thumbImage2;

    public void setThumbnail(ImageView v) {
        thumbImage2 = v;
    }

    public TextView secondTitle;
    public TextView secondSubTitle;
    public View wrapArea;

    public void setWrapArea(View v) {
        wrapArea = v;
        setSecondTitle((TextView) v.findViewById(R.id.contenttitle));
        setSecondSubtitle((TextView) v.findViewById(R.id.contenturl));

    }

    public void setSecondTitle(TextView v) {
        secondTitle = v;
    }

    public void setSecondSubtitle(TextView v) {
        secondSubTitle = v;
    }

    public void setUrl(String url) {

    }

    private void init() {
        inflate(getContext(), R.layout.header_image_title_view, this);
        this.title = (TextView) findViewById(R.id.textimage);
        this.info = (TextView) findViewById(R.id.subtextimage);
        this.backdrop = (ImageView) findViewById(R.id.leadimage);
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}

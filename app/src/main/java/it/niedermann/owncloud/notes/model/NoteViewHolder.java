package it.niedermann.owncloud.notes.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.niedermann.owncloud.notes.R;
import it.niedermann.owncloud.notes.branding.BrandingUtil;
import it.niedermann.owncloud.notes.databinding.ItemNotesListNoteItemBinding;
import it.niedermann.owncloud.notes.util.Notes;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static it.niedermann.owncloud.notes.util.ColorUtil.contrastRatioIsSufficient;
import static it.niedermann.owncloud.notes.util.ColorUtil.isColorDark;

public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
    private final ItemNotesListNoteItemBinding binding;
    private final NoteClickListener noteClickListener;

    public NoteViewHolder(View v, NoteClickListener noteClickListener) {
        super(v);
        binding = ItemNotesListNoteItemBinding.bind(v);
        this.noteClickListener = noteClickListener;
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int adapterPosition = getAdapterPosition();
        if (adapterPosition != NO_POSITION) {
            noteClickListener.onNoteClick(adapterPosition, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return noteClickListener.onNoteLongClick(getAdapterPosition(), v);
    }

    public void showSwipe(boolean left) {
        binding.noteFavoriteLeft.setVisibility(left ? View.VISIBLE : View.INVISIBLE);
        binding.noteDeleteRight.setVisibility(left ? View.INVISIBLE : View.VISIBLE);
        binding.noteSwipeFrame.setBackgroundResource(left ? R.color.bg_warning : R.color.bg_attention);
    }

    public void bind(DBNote note, NoteClickListener noteClickListener, boolean showCategory, int mainColor, int textColor, @Nullable CharSequence searchQuery) {
        @NonNull final Context context = itemView.getContext();
        final boolean isDarkThemeActive = Notes.isDarkThemeActive(context);

        binding.noteSwipeable.setAlpha(DBStatus.LOCAL_DELETED.equals(note.getStatus()) ? 0.5f : 1.0f);

        binding.noteCategory.setVisibility(showCategory && !note.getCategory().isEmpty() ? View.VISIBLE : View.GONE);
        binding.noteCategory.setText(note.getCategory());

        @ColorInt int categoryForeground;
        @ColorInt int categoryBackground;

        if (isDarkThemeActive) {
            if (isColorDark(mainColor)) {
                if (contrastRatioIsSufficient(mainColor, Color.BLACK)) {
                    categoryBackground = mainColor;
                    categoryForeground = Color.WHITE;
                } else {
                    categoryBackground = Color.WHITE;
                    categoryForeground = mainColor;
                }
            } else {
                categoryBackground = mainColor;
                categoryForeground = Color.BLACK;
            }
        } else {
            categoryForeground = Color.BLACK;
            if (isColorDark(mainColor) || contrastRatioIsSufficient(mainColor, Color.WHITE)) {
                categoryBackground = mainColor;
            } else {
                categoryBackground = Color.BLACK;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(binding.noteCategory.getBackground(), categoryBackground);
        } else {
            final GradientDrawable drawable = (GradientDrawable) binding.noteCategory.getBackground();
            drawable.setStroke(1, categoryBackground);
            drawable.setColor(isDarkThemeActive ? categoryBackground : Color.TRANSPARENT);
        }
        binding.noteCategory.setTextColor(categoryForeground);

        binding.noteStatus.setVisibility(DBStatus.VOID.equals(note.getStatus()) ? View.INVISIBLE : View.VISIBLE);
        binding.noteFavorite.setImageResource(note.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_grey_ccc_24dp);
        binding.noteFavorite.setOnClickListener(view -> noteClickListener.onNoteFavoriteClick(getAdapterPosition(), view));

        if (!TextUtils.isEmpty(searchQuery)) {
            @ColorInt final int searchBackground = context.getResources().getColor(R.color.bg_highlighted);
            @ColorInt final int searchForeground = BrandingUtil.getSecondaryForegroundColorDependingOnTheme(context, mainColor);

            final Pattern pattern = Pattern.compile("(" + searchQuery + ")", Pattern.CASE_INSENSITIVE);
            SpannableString spannableString = new SpannableString(note.getTitle());
            Matcher matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            binding.noteTitle.setText(spannableString);

            spannableString = new SpannableString(note.getExcerpt());
            matcher = pattern.matcher(spannableString);
            while (matcher.find()) {
                spannableString.setSpan(new ForegroundColorSpan(searchForeground), matcher.start(), matcher.end(), 0);
                spannableString.setSpan(new BackgroundColorSpan(searchBackground), matcher.start(), matcher.end(), 0);
            }

            binding.noteExcerpt.setText(spannableString);
        } else {
            binding.noteTitle.setText(note.getTitle());
            binding.noteExcerpt.setText(note.getExcerpt());
        }
    }

    public View getNoteSwipeable() {
        return binding.noteSwipeable;
    }
}
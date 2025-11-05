package com.example.carbon;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class InvitationDialog {
    private final Dialog dialog;

    public InvitationDialog(Context context,Notification notification, NotificationService service, View parentView) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_invitation);

        TextView eventTitle = dialog.findViewById(R.id.invite_event_title);
        TextView message = dialog.findViewById(R.id.invite_message);
        ImageView avatar = dialog.findViewById(R.id.invite_avatar);
        Button acceptBtn = dialog.findViewById(R.id.btn_accept);
        Button declineBtn = dialog.findViewById(R.id.btn_decline);

        eventTitle.setText(notification.getEventName());
        message.setText(notification.getMessage());

        acceptBtn.setOnClickListener(v -> {
            service.markAsAccepted(notification, () -> {
                Snackbar.make(parentView, "Invitation Accepted!", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            }, e -> Snackbar.make(parentView, "Error Accepting Invitation", Snackbar.LENGTH_SHORT).show());
        });

        declineBtn.setOnClickListener(v -> {
            service.markAsDeclined(notification, () -> {
                Snackbar.make(parentView, "Invitation Declined", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();

            }, e -> Snackbar.make(parentView, "Error Declining Invitation", Snackbar.LENGTH_SHORT).show());
        });
    }

    public void show() {
        dialog.show();
    }
}

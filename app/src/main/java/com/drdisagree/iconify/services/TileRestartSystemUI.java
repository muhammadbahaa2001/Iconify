package com.drdisagree.iconify.services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;

public class TileRestartSystemUI extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(Tile.STATE_INACTIVE);
        pitchBlackTile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        SystemUtil.restartSystemUI();

        Tile pitchBlackTile = getQsTile();
        pitchBlackTile.setState(Tile.STATE_INACTIVE);
        pitchBlackTile.setLabel(getResources().getString(R.string.restart_sysui_title));
        pitchBlackTile.setSubtitle("");
        pitchBlackTile.updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}

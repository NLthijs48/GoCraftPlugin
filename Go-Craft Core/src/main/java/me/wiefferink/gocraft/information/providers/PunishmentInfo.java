package me.wiefferink.gocraft.information.providers;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;

import java.sql.SQLException;
import java.util.UUID;

public class PunishmentInfo extends InformationProvider {


    @Override
    public void showAsync(InformationRequest request) {
        BanManager manager = plugin.getBanManagerLink().get();
        try {
            UUID uuid = request.getAboutOffline().getUniqueId();
            PlayerData playerData = BmAPI.getPlayer(uuid);

            if (BmAPI.isBanned(uuid)) {
                PlayerBanData ban = BmAPI.getCurrentBan(uuid);
                request.message(Message.fromKey("information-punishments-currentban").replacements(Utils.agoMessage(ban.getExpires()), ban.getReason()));
            }

            if (BmAPI.isMuted(uuid)) {
                PlayerBanData mute = BmAPI.getCurrentBan(uuid);
                request.message(Message.fromKey("information-punishments-currentmute").replacements(Utils.agoMessage(mute.getExpires()), mute.getReason()));
            }

            long banCount = manager.getPlayerBanRecordStorage().getCount(playerData);
            long kickCount = manager.getPlayerKickStorage().getCount(playerData);
            long muteCount = manager.getPlayerMuteRecordStorage().getCount(playerData);

            if (banCount != 0) {
                request.message(Message.fromKey("information-punishments-bancount").replacements(banCount));
            }

            if (kickCount != 0) {
                request.message(Message.fromKey("information-punishments-kickcount").replacements(kickCount));
            }

            if (muteCount != 0) {
                request.message(Message.fromKey("information-punishments-mutecount").replacements(muteCount));
            }

        } catch (SQLException e) {
            Log.error("Failed to get ban/kick/mute data from BanManager", e);
        }

    }
}

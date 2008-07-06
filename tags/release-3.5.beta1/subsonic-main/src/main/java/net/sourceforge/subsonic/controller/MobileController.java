package net.sourceforge.subsonic.controller;

import net.sourceforge.subsonic.domain.MusicFile;
import net.sourceforge.subsonic.domain.MusicIndex;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.service.MusicFileService;
import net.sourceforge.subsonic.service.MusicIndexService;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SettingsService;
import net.sourceforge.subsonic.service.TranscodingService;
import net.sourceforge.subsonic.util.StringUtil;
import net.sourceforge.subsonic.util.XMLBuilder;
import static net.sourceforge.subsonic.util.XMLBuilder.Attribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Multi-controller used for mobile phone pages.
 *
 * @author Sindre Mehus
 */
public class MobileController extends MultiActionController {

    private SettingsService settingsService;
    private PlayerService playerService;
    private MusicFileService musicFileService;
    private MusicIndexService musicIndexService;
    private TranscodingService transcodingService;

    public ModelAndView getIndexes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/xml");
        response.setCharacterEncoding(StringUtil.ENCODING_UTF8);
        PrintWriter out = response.getWriter();
        SortedMap<MusicIndex, SortedSet<MusicIndex.Artist>> indexedArtists = musicIndexService.getIndexedArtists(settingsService.getAllMusicFolders());

        XMLBuilder builder = new XMLBuilder();
        builder.preamble("UTF-8");
        builder.add("indexes");

        for (Map.Entry<MusicIndex, SortedSet<MusicIndex.Artist>> entry : indexedArtists.entrySet()) {
            builder.add("index", "name", entry.getKey().getIndex());
            for (MusicIndex.Artist artist : entry.getValue()) {
                for (MusicFile musicFile : artist.getMusicFiles()) {
                    if (musicFile.isDirectory()) {
                        builder.add("artist", new Attribute("name", artist.getName()), new Attribute("path", StringUtil.utf8HexEncode(musicFile.getPath())));
                        builder.end();
                    }
                }
            }
            builder.end();
        }
        builder.end();
        out.print(builder);

        return null;
    }

    public ModelAndView getMusicDirectory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Player player = playerService.getPlayer(request, response);

        MusicFile musicFile = musicFileService.getMusicFile(request.getParameter("path"));
        String baseUrl = getBaseUrl(request);

        // TODO: Share code with M3UController.
        // TODO: Make it work with SSL.
        response.setContentType("text/xml");
        response.setCharacterEncoding(StringUtil.ENCODING_UTF8);
        PrintWriter out = response.getWriter();

        XMLBuilder builder = new XMLBuilder();
        builder.preamble("UTF-8");
        builder.add("directory", new Attribute("name", musicFile.getName()), new Attribute("path", StringUtil.utf8HexEncode(musicFile.getPath())));

        // TODO: Do not include contentType and URL if directory.
        for (MusicFile child : musicFile.getChildren(true, true)) {
            String suffix = transcodingService.getSuffix(player, child);
            String contentType = StringUtil.getMimeType(suffix);
            String url = baseUrl + "stream?pathUtf8Hex=" + StringUtil.utf8HexEncode(child.getPath()) + "&mobile";
            String path = StringUtil.utf8HexEncode(child.getPath());

            builder.add("child", new Attribute("name", child.getTitle()),
                        new Attribute("path", path), new Attribute("isDir", child.isDirectory()),
                        new Attribute("contentType", contentType), new Attribute("url", url));
            builder.end();
        }

        builder.end();
        out.print(builder);

        return null;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString();
        baseUrl = baseUrl.replaceFirst("/mobile.*", "/");

        // Rewrite URLs in case we're behind a proxy.
        if (settingsService.isRewriteUrlEnabled()) {
            String referer = request.getHeader("referer");
            baseUrl = StringUtil.rewriteUrl(baseUrl, referer);
        }
        return baseUrl;
    }

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setMusicFileService(MusicFileService musicFileService) {
        this.musicFileService = musicFileService;
    }

    public void setMusicIndexService(MusicIndexService musicIndexService) {
        this.musicIndexService = musicIndexService;
    }

    public void setTranscodingService(TranscodingService transcodingService) {
        this.transcodingService = transcodingService;
    }
}
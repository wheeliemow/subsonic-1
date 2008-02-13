package net.sourceforge.subsonic.controller;

import net.sourceforge.subsonic.domain.MusicFile;
import net.sourceforge.subsonic.domain.Player;
import net.sourceforge.subsonic.domain.Playlist;
import net.sourceforge.subsonic.domain.RandomSearchCriteria;
import net.sourceforge.subsonic.service.PlayerService;
import net.sourceforge.subsonic.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the creating a random playlist.
 *
 * @author Sindre Mehus
 */
public class RandomPlaylistController extends ParameterizableViewController {

    private SearchService searchService;
    private PlayerService playerService;
    private List<ReloadFrame> reloadFrames;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        int size = ServletRequestUtils.getRequiredIntParameter(request, "size");
        String genre = request.getParameter("genre");
        if (StringUtils.equalsIgnoreCase("any", genre)) {
            genre = null;
        }

        Integer fromYear = null;
        Integer toYear = null;

        String year = request.getParameter("year");
        if (!StringUtils.equalsIgnoreCase("any", year)) {
            String[] tmp = StringUtils.split(year);
            fromYear = Integer.parseInt(tmp[0]);
            toYear = Integer.parseInt(tmp[1]);
        }

        Player player = playerService.getPlayer(request, response);
        Playlist playlist = player.getPlaylist();
        playlist.clear();

        RandomSearchCriteria criteria = new RandomSearchCriteria(size, genre, fromYear, toYear);
        List<MusicFile> randomFiles = searchService.getRandomSongs(criteria);
        for (MusicFile randomFile : randomFiles) {
            playlist.addFile(randomFile);
        }

        if (request.getParameter("autoRandom") != null) {
            playlist.setRandomSearchCriteria(criteria);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("reloadFrames", reloadFrames);

        ModelAndView result = super.handleRequestInternal(request, response);
        result.addObject("model", map);
        return result;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setReloadFrames(List<ReloadFrame> reloadFrames) {
        this.reloadFrames = reloadFrames;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }
}

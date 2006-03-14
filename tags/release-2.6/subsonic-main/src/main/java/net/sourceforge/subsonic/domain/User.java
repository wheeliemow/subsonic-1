package net.sourceforge.subsonic.domain;

/**
 * Represent a user.
 *
 * @author Sindre Mehus
 * @version $Revision: 1.3 $ $Date: 2006/01/12 15:22:51 $
 */
public class User {

    public static final String USERNAME_ADMIN = "admin";

    private String username;
    private String password;

    private boolean isAdminRole;
    private boolean isDownloadRole;
    private boolean isUploadRole;
    private boolean isPlaylistRole;
    private boolean isCoverArtRole;
    private boolean isCommentRole;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdminRole() {
        return isAdminRole;
    }

    public void setAdminRole(boolean isAdminRole) {
        this.isAdminRole = isAdminRole;
    }

    public boolean isCommentRole() {
        return isCommentRole;
    }

    public void setCommentRole(boolean isCommentRole) {
        this.isCommentRole = isCommentRole;
    }

    public boolean isDownloadRole() {
        return isDownloadRole;
    }

    public void setDownloadRole(boolean isDownloadRole) {
        this.isDownloadRole = isDownloadRole;
    }

    public boolean isUploadRole() {
        return isUploadRole;
    }

    public void setUploadRole(boolean isUploadRole) {
        this.isUploadRole = isUploadRole;
    }

    public boolean isPlaylistRole() {
        return isPlaylistRole;
    }

    public void setPlaylistRole(boolean isPlaylistRole) {
        this.isPlaylistRole = isPlaylistRole;
    }

    public boolean isCoverArtRole() {
        return isCoverArtRole;
    }

    public void setCoverArtRole(boolean isCoverArtRole) {
        this.isCoverArtRole = isCoverArtRole;
    }

    public String toString() {
        StringBuffer result = new StringBuffer(username);

        if (isAdminRole) {
            result.append(" [admin]");
        }
        if (isDownloadRole) {
            result.append(" [download]");
        }
        if (isUploadRole) {
            result.append(" [upload]");
        }
        if (isPlaylistRole) {
            result.append(" [playlist]");
        }
        if (isCoverArtRole) {
            result.append(" [coverart]");
        }
        if (isCommentRole) {
            result.append(" [comment]");
        }

        return result.toString();
    }
}

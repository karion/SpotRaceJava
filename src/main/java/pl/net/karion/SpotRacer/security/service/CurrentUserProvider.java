package pl.net.karion.SpotRacer.security.service;

import pl.net.karion.SpotRacer.security.model.CurrentUser;

public interface CurrentUserProvider {

    CurrentUser currentUser();
}

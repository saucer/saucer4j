package co.casterlabs.saucer;

import co.casterlabs.rakurai.json.Rson;

public class Test {

    public static void main(String[] args) {
        SaucerView view = SaucerView.create(
            new SaucerOptions()
                .hardwareAcceleration(true)
        );

        view.setDevtoolsVisible(true);
        view.setContextMenuAllowed(true);
        view.setUrl("https://example.com");

        view.addFunction("heySaucerWhatsMyUrl", (funcArgs) -> {
            return Rson.DEFAULT.toJson(view.currentUrl());
        });

        view.addFunction("setBackground", (funcArgs) -> {
            view.setBackground(
                new SaucerColor(
                    funcArgs.getNumber(0).intValue(),
                    funcArgs.getNumber(1).intValue(),
                    funcArgs.getNumber(2).intValue(),
                    funcArgs.getNumber(3).intValue()
                )
            );
            return null;
        });

        view.addFunction("throwAnError", (funcArgs) -> {
            throw new Exception("An error :D");
        });

        view.run();
    }

}

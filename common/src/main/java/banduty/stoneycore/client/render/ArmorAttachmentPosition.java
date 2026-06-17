package banduty.stoneycore.client.render;

public interface ArmorAttachmentPosition {
    default float getOffsetX() {
        return 0;
    }

    default float getOffsetY() {
        return 0;
    }

    default float getOffsetZ() {
        return 0;
    }
}
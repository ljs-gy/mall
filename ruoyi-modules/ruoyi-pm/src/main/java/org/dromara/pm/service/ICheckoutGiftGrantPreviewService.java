package org.dromara.pm.service;

import org.dromara.pm.api.domain.RemoteGiftGrantPreviewBo;
import org.dromara.pm.api.domain.RemoteGiftGrantRewardBo;

import java.util.List;

/**
 * 满额送券预告
 */
public interface ICheckoutGiftGrantPreviewService {

    List<RemoteGiftGrantRewardBo> preview(RemoteGiftGrantPreviewBo bo);
}

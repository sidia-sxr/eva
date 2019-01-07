/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package br.org.sidia.eva.mode.photo;

import br.org.sidia.eva.PetContext;
import br.org.sidia.eva.R;
import br.org.sidia.eva.view.BaseViewController;

public class PhotoViewController extends BaseViewController {

    public PhotoViewController(PetContext petContext) {
        super(petContext, R.layout.view_main_content_transparent);
        registerView(IPhotoView.class, R.layout.view_photo, PhotoView.class);
    }
}

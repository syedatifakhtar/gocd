##########################################################################
# Copyright 2017 ThoughtWorks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################################################################

module ConfigView
  class TemplatesController < ConfigView::ConfigViewController
    before_action :is_user_authorized_to_view_template

    def show
      result = HttpLocalizedOperationResult.new
      @template_config = template_config_service.loadForView(params[:name], result)
      render_localized_operation_result(result) unless result.isSuccessful()
    end

    private
    def is_user_authorized_to_view_template
      unless security_service.isAuthorizedToViewTemplate(params[:name], current_user)
        render 'shared/config_error'
      end
    end
  end
end

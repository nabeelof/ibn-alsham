<?php

namespace App\Traits;

use Illuminate\Http\JsonResponse;

trait ApiResponse
{
    protected function success(
        mixed $data = null,
        array $pagination = [],
        string $message = '',
        int $status = 200
    ): JsonResponse {
        $response = ['success' => true];

        if ($message) {
            $response['message'] = $message;
        }

        if (!is_null($data)) {
            $response['data'] = $data;
        }

        if (!empty($pagination)) {
            $response['pagination'] = $pagination;
        }

        return response()->json($response, $status);
    }

    protected function error(
        string $message,
        int $status = 400,
        mixed $errors = null
    ): JsonResponse {
        $response = [
            'success' => false,
            'message' => $message,
        ];

        if (!is_null($errors)) {
            $response['errors'] = $errors;
        }

        return response()->json($response, $status);
    }
}
